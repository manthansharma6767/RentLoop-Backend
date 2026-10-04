package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private User renter;
    private User owner;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        renter = new User();
        renter.setId(2L);
        renter.setEmail("renter@example.com");

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setItem(item);

        booking = new Booking();
        booking.setId(500L);
        booking.setListing(listing);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.now().minusDays(5));
        booking.setEndDate(LocalDate.now().minusDays(2));
        booking.setStatus(BookingStatus.RETURNED);
    }

    @Test
    void saveReview_Success_RenterReviewsOwner_AfterReturned() {
        ReviewDto inputDto = ReviewDto.builder()
                .bookingId(500L)
                .reviewerEmail("hacker@example.com") // Spoofed email from client
                .rating(5)
                .comment("Great item!")
                .build();

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(500L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        ReviewDto result = reviewService.saveReview(inputDto, "renter@example.com");

        assertNotNull(result);
        assertEquals("renter@example.com", result.getReviewerEmail()); // Spoofed email ignored, authenticated user used
        assertEquals("owner@example.com", result.getTargetEmail());   // Derived automatically
        assertEquals(5, result.getRating());

        verify(notificationService, times(1)).createAndSend(eq("owner@example.com"), anyString(), eq(NotificationType.NEW_REVIEW));
    }

    @Test
    void saveReview_Fails_WhenBookingNotReturned() {
        booking.setStatus(BookingStatus.ACTIVE); // Not completed
        ReviewDto inputDto = ReviewDto.builder().bookingId(500L).rating(4).comment("Good").build();

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reviewService.saveReview(inputDto, "renter@example.com")
        );
        assertTrue(ex.getMessage().contains("RETURNED status"));
    }

    @Test
    void saveReview_Fails_WhenUserNotParticipant() {
        ReviewDto inputDto = ReviewDto.builder().bookingId(500L).rating(4).comment("Good").build();

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                reviewService.saveReview(inputDto, "outsider@example.com")
        );
        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    void saveReview_Fails_OnDuplicateReview() {
        ReviewDto inputDto = ReviewDto.builder().bookingId(500L).rating(4).comment("Good").build();

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(500L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reviewService.saveReview(inputDto, "renter@example.com")
        );
        assertTrue(ex.getMessage().contains("already been submitted"));
    }
}
