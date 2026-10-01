package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTargetEmail(String targetEmail);

    List<Review> findByListingId(Long listingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetEmail = :targetEmail")
    Double findAverageRatingByTargetEmail(@Param("targetEmail") String targetEmail);

    boolean existsByBookingId(Long bookingId);
}
