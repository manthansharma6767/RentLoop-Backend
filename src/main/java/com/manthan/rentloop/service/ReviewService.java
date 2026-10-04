package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserReviewsResponse;
import com.manthan.rentloop.model.Booking;
import com.manthan.rentloop.model.BookingStatus;
import com.manthan.rentloop.model.NotificationType;
import com.manthan.rentloop.model.Review;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewDto saveReview(ReviewDto reviewDto, String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new AccessDeniedException("User must be authenticated to submit a review.");
        }

        // B. Verify booking exists
        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + reviewDto.getBookingId()));

        // C. Verify booking status is RETURNED (completed)
        if (booking.getStatus() != BookingStatus.RETURNED) {
            throw new IllegalArgumentException("Reviews can only be submitted for completed rentals (RETURNED status).");
        }

        // D. Verify authenticated user participated in the booking (renter OR owner)
        String renterEmail = booking.getRenter().getEmail();
        String ownerEmail = booking.getListing().getItem().getOwner().getEmail();

        boolean isRenter = authenticatedEmail.equals(renterEmail);
        boolean isOwner = authenticatedEmail.equals(ownerEmail);

        if (!isRenter && !isOwner) {
            throw new AccessDeniedException("You are not authorized to submit a review for this booking.");
        }

        // F. One-review-per-booking constraint
        if (reviewRepository.existsByBookingId(reviewDto.getBookingId())) {
            throw new IllegalArgumentException("A review has already been submitted for booking ID: " + reviewDto.getBookingId());
        }

        // Target email is automatically derived as the other participant
        String targetEmail = isRenter ? ownerEmail : renterEmail;

        // A. Always derive reviewer identity server-side from authenticated principal
        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setReviewerEmail(authenticatedEmail);
        review.setTargetEmail(targetEmail);
        review.setListingId(booking.getListing().getId());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review saved = reviewRepository.save(review);

        // Notify target user
        String notificationMessage = String.format("You received a new %d-star review from %s.", saved.getRating(), saved.getReviewerEmail());
        notificationService.createAndSend(saved.getTargetEmail(), notificationMessage, NotificationType.NEW_REVIEW);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserReviewsResponse getReviewsForUser(String targetEmail) {
        List<Review> reviews = reviewRepository.findByTargetEmail(targetEmail);
        Double avgRating = reviewRepository.findAverageRatingByTargetEmail(targetEmail);
        if (avgRating == null) {
            avgRating = 0.0;
        }

        List<ReviewDto> reviewDtos = reviews.stream().map(this::toDto).toList();

        return UserReviewsResponse.builder()
                .targetEmail(targetEmail)
                .averageRating(avgRating)
                .reviews(reviewDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForListing(Long listingId) {
        List<Review> reviews = reviewRepository.findByListingId(listingId);
        return reviews.stream().map(this::toDto).toList();
    }

    private ReviewDto toDto(Review review) {
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

