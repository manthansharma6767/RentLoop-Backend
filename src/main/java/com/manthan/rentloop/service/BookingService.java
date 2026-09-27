package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.BookingRequest;
import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ListingRepository;
import com.manthan.rentloop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        User renter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("This listing is not currently active.");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // Prevent the owner from booking their own listing
        if (listing.getItem().getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You cannot book your own listing.");
        }

        // Strict date availability check — same overlap logic used in search
        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                listing.getId(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.ACTIVE),
                request.getStartDate(),
                request.getEndDate()
        );
        if (hasConflict) {
            throw new IllegalArgumentException("The listing is not available for the selected dates.");
        }

        // Calculate total amount: number of days × pricePerDay
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days <= 0) {
            days = 1; // Minimum 1-day rental for same-day bookings
        }
        BigDecimal totalAmount = listing.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setListing(listing);
        booking.setRenter(renter);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(listing.getDepositAmount());
        booking.setStatus(BookingStatus.REQUESTED);

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse approveBooking(String ownerEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // OWNERSHIP AUTHORIZATION CHECK — only the item owner can approve
        if (!booking.getListing().getItem().getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("You are not authorized to approve this booking.");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new IllegalArgumentException("Only bookings with status REQUESTED can be approved.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Both the renter and the item owner can cancel
        boolean isRenter = booking.getRenter().getEmail().equals(userEmail);
        boolean isOwner = booking.getListing().getItem().getOwner().getEmail().equals(userEmail);

        if (!isRenter && !isOwner) {
            throw new IllegalArgumentException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("This booking is already cancelled.");
        }

        if (booking.getStatus() == BookingStatus.RETURNED) {
            throw new IllegalArgumentException("Cannot cancel a completed booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyRentals(String renterEmail) {
        return bookingRepository.findByRenterEmail(renterEmail)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsOnMyItems(String ownerEmail) {
        return bookingRepository.findByListingItemOwnerEmail(ownerEmail)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setListingId(booking.getListing().getId());
        response.setListingTitle(booking.getListing().getTitle());
        response.setRenterName(booking.getRenter().getName());
        response.setOwnerName(booking.getListing().getItem().getOwner().getName());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setTotalAmount(booking.getTotalAmount());
        response.setDepositAmount(booking.getDepositAmount());
        response.setStatus(booking.getStatus().name());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}
