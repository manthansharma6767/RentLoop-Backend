package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.Booking;
import com.manthan.rentloop.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Bookings where I am the renter */
    List<Booking> findByRenterEmail(String email);

    /** Booking requests made by others on my items */
    List<Booking> findByListingItemOwnerEmail(String email);

    /** Check for overlapping CONFIRMED or ACTIVE bookings on a listing within a date range */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.listing.id = :listingId " +
            "AND b.status IN :statuses " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate")
    boolean existsOverlappingBooking(
            @Param("listingId") Long listingId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
