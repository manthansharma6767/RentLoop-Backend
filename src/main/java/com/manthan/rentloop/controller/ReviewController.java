package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserReviewsResponse;
import com.manthan.rentloop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewDto reviewDto) {
        String authenticatedEmail = userDetails != null ? userDetails.getUsername() : null;
        ReviewDto created = reviewService.saveReview(reviewDto, authenticatedEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<UserReviewsResponse> getReviewsForUser(@PathVariable("email") String email) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(email));
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForListing(@PathVariable("listingId") Long listingId) {
        return ResponseEntity.ok(reviewService.getReviewsForListing(listingId));
    }
}
