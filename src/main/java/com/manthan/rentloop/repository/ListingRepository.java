package com.manthan.rentloop.repository;
import com.manthan.rentloop.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByItemOwnerEmail(String email);

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR l.item.category.id = :categoryId) " +
            "AND (:minPrice IS NULL OR l.pricePerDay >= :minPrice) " +
            "AND (:maxPrice IS NULL OR l.pricePerDay <= :maxPrice) " +
            "AND (" +
            "  CAST(:startDate AS date) IS NULL OR CAST(:endDate AS date) IS NULL OR " +
            "  NOT EXISTS (" +
            "    SELECT b FROM Booking b WHERE b.listing = l " +
            "    AND b.status IN ('CONFIRMED', 'ACTIVE') " +
            "    AND b.startDate <= :endDate AND b.endDate >= :startDate" +
            "  )" +
            ")")
    Page<Listing> searchListings(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}