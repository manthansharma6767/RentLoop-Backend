package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.BookingRequest;
import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ListingRepository;
import com.manthan.rentloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private User renter;
    private User owner;
    private Listing listing;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setName("Owner Name");

        renter = new User();
        renter.setId(2L);
        renter.setEmail("renter@example.com");
        renter.setName("Renter Name");

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        listing = new Listing();
        listing.setId(100L);
        listing.setItem(item);
        listing.setTitle("Test Camera");
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setPricePerDay(BigDecimal.valueOf(25.00));
        listing.setDepositAmount(BigDecimal.valueOf(50.00));

        booking = new Booking();
        booking.setId(500L);
        booking.setListing(listing);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REQUESTED);
    }

    @Test
    void createBooking_Success_UsesPessimisticLock() {
        BookingRequest request = new BookingRequest();
        request.setListingId(100L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findByEmail("renter@example.com")).thenReturn(Optional.of(renter));
        when(listingRepository.findByIdWithLock(100L)).thenReturn(Optional.of(listing));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(500L);
            return b;
        });

        BookingResponse response = bookingService.createBooking("renter@example.com", request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("REQUESTED", response.getStatus());

        // Verify pessimistic lock query was invoked
        verify(listingRepository, times(1)).findByIdWithLock(100L);
        // Verify notification sent to owner
        verify(notificationService, times(1)).createAndSend(eq("owner@example.com"), anyString(), eq(NotificationType.BOOKING_UPDATE));
    }

    @Test
    void approveBooking_Success_RequestedToConfirmed() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse response = bookingService.approveBooking("owner@example.com", 500L);

        assertEquals("CONFIRMED", response.getStatus());
        verify(notificationService, times(1)).createAndSend(eq("renter@example.com"), anyString(), eq(NotificationType.BOOKING_UPDATE));
    }

    @Test
    void startBooking_Success_ConfirmedToActive() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse response = bookingService.startBooking("owner@example.com", 500L);

        assertEquals("ACTIVE", response.getStatus());
        verify(notificationService, times(1)).createAndSend(eq("renter@example.com"), anyString(), eq(NotificationType.BOOKING_UPDATE));
    }

    @Test
    void startBooking_Fails_WhenNotInConfirmedState() {
        booking.setStatus(BookingStatus.REQUESTED); // Illegal transition: REQUESTED -> ACTIVE
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.startBooking("owner@example.com", 500L)
        );

        assertTrue(ex.getMessage().contains("Only bookings with status CONFIRMED can be started"));
    }

    @Test
    void returnBooking_Success_ActiveToReturned() {
        booking.setStatus(BookingStatus.ACTIVE);
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse response = bookingService.returnBooking("owner@example.com", 500L);

        assertEquals("RETURNED", response.getStatus());
        verify(notificationService, times(1)).createAndSend(eq("renter@example.com"), anyString(), eq(NotificationType.BOOKING_UPDATE));
    }

    @Test
    void returnBooking_Fails_WhenNotInActiveState() {
        booking.setStatus(BookingStatus.CONFIRMED); // Illegal transition: CONFIRMED -> RETURNED
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.returnBooking("owner@example.com", 500L)
        );

        assertTrue(ex.getMessage().contains("Only bookings with status ACTIVE can be marked as returned"));
    }

    @Test
    void bookingTransitions_UnauthorizedUser_Fails() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        // Unrelated user tries to approve
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.approveBooking("unauthorized@example.com", 500L)
        );
    }
}
