package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.RequestMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestMatchRepository extends JpaRepository<RequestMatch, Long> {

    List<RequestMatch> findByRentalRequestId(Long rentalRequestId);

    boolean existsByRentalRequestIdAndListingId(Long rentalRequestId, Long listingId);
}
