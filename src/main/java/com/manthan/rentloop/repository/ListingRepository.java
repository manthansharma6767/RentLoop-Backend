package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.Listing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByItemOwnerEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.id = :id")
    Optional<Listing> findByIdWithLock(@Param("id") Long id);

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

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' " +
            "AND l.item.category.id = :categoryId " +
            "AND l.pricePerDay <= :budgetPerDay " +
            "AND NOT EXISTS (" +
            "  SELECT b FROM Booking b WHERE b.listing = l " +
            "  AND b.status IN ('CONFIRMED', 'ACTIVE') " +
            "  AND b.startDate <= :endDate AND b.endDate >= :startDate" +
            ")")
    List<Listing> findMatchingListings(
            @Param("categoryId") Long categoryId,
            @Param("budgetPerDay") BigDecimal budgetPerDay,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}