package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private Review review;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setBookingId(500L);
        review.setReviewerEmail("renter@reviewrepo.com");
        review.setTargetEmail("owner@reviewrepo.com");
        review.setListingId(100L);
        review.setRating(5);
        review.setComment("Excellent experience!");
        review = reviewRepository.save(review);
    }

    @Test
    void existsByBookingId_ReturnsTrue_WhenReviewExists() {
        assertTrue(reviewRepository.existsByBookingId(500L));
        assertFalse(reviewRepository.existsByBookingId(999L));
    }

    @Test
    void findByTargetEmail_ReturnsReviewsForTargetUser() {
        List<Review> reviews = reviewRepository.findByTargetEmail("owner@reviewrepo.com");

        assertEquals(1, reviews.size());
        assertEquals("renter@reviewrepo.com", reviews.get(0).getReviewerEmail());
    }

    @Test
    void findByListingId_ReturnsReviewsForListing() {
        List<Review> reviews = reviewRepository.findByListingId(100L);

        assertEquals(1, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
    }
}
