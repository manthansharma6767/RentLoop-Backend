package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserReviewsResponse;
import com.manthan.rentloop.model.NotificationType;
import com.manthan.rentloop.model.Review;
import com.manthan.rentloop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewDto saveReview(ReviewDto reviewDto, String authenticatedEmail) {
        if (reviewRepository.existsByBookingId(reviewDto.getBookingId())) {
            throw new IllegalArgumentException("A review has already been submitted for booking ID: " + reviewDto.getBookingId());
        }

        String reviewerEmail = reviewDto.getReviewerEmail();
        if (reviewerEmail == null || reviewerEmail.isBlank()) {
            reviewerEmail = authenticatedEmail;
        }

        Review review = new Review();
        review.setBookingId(reviewDto.getBookingId());
        review.setReviewerEmail(reviewerEmail);
        review.setTargetEmail(reviewDto.getTargetEmail());
        review.setListingId(reviewDto.getListingId());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review saved = reviewRepository.save(review);

        // Crucial: Create and dispatch notification to targetEmail
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
