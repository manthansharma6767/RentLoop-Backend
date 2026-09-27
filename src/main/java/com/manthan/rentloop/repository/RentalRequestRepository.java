package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {

    List<RentalRequest> findByRequesterEmail(String email);
}
