package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.model.ListingStatus;
import com.manthan.rentloop.model.Listing;
import com.manthan.rentloop.model.ListingImage;
import com.manthan.rentloop.model.Review;
import com.manthan.rentloop.model.RentalRequest;
import com.manthan.rentloop.model.Booking;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ListingRepository;
import com.manthan.rentloop.repository.RentalRequestRepository;
import com.manthan.rentloop.repository.ReviewRepository;
import com.manthan.rentloop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin-only service providing cross-user visibility and moderation capabilities.
 * No user identity is trusted from request body; admin identity is verified at the controller layer.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final ReviewRepository reviewRepository;
    private final ItemService itemService;

    // ─── USER MANAGEMENT ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        return mapUserToDto(user);
    }

    // ─── LISTING MODERATION ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ListingResponse> getAllListings() {
        return listingRepository.findAll()
                .stream()
                .map(this::mapListingToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin force-removes a listing regardless of ownership.
     * Uses the existing soft-delete pattern (status → REMOVED).
     */
    @Transactional
    public void adminRemoveListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + listingId));
        listing.setStatus(ListingStatus.REMOVED);
        listingRepository.save(listing);
    }

    // ─── BOOKING OVERSIGHT ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
    }

    // ─── RENTAL REQUEST OVERSIGHT ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RentalRequestResponse> getAllRentalRequests() {
        return rentalRequestRepository.findAll()
                .stream()
                .map(this::mapRentalRequestToResponse)
                .collect(Collectors.toList());
    }

    // ─── REVIEW MODERATION ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::mapReviewToDto)
                .collect(Collectors.toList());
    }

    /**
     * Admin hard-deletes a review (inappropriate content removal).
     */
    @Transactional
    public void adminDeleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("Review not found with ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    // ─── PRIVATE MAPPERS ─────────────────────────────────────────────────────

    private UserProfileDto mapUserToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private ListingResponse mapListingToResponse(Listing listing) {
        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setPricePerDay(listing.getPricePerDay());
        response.setDepositAmount(listing.getDepositAmount());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        response.setStatus(listing.getStatus().name());
        response.setItem(itemService.mapToResponse(listing.getItem()));
        if (listing.getImages() != null) {
            response.setImageUrls(listing.getImages().stream()
                    .map(ListingImage::getImageUrl).collect(Collectors.toList()));
        }
        return response;
    }

    private BookingResponse mapBookingToResponse(Booking booking) {
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

    private RentalRequestResponse mapRentalRequestToResponse(RentalRequest request) {
        RentalRequestResponse response = new RentalRequestResponse();
        response.setId(request.getId());
        response.setRequesterName(request.getRequester().getName());
        response.setCategoryName(request.getCategory().getName());
        response.setDescription(request.getDescription());
        response.setBudgetPerDay(request.getBudgetPerDay());
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());
        response.setStatus(request.getStatus().name());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }

    private ReviewDto mapReviewToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .bookingId(review.getBookingId())
                .reviewerEmail(review.getReviewerEmail())
                .targetEmail(review.getTargetEmail())
                .listingId(review.getListingId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
