package com.manthan.rentloop.repository;
import com.manthan.rentloop.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    // Custom query to find all listings belonging to a specific user's items
    List<Listing> findByItemOwnerEmail(String email);
}
