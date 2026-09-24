package com.manthan.rentloop.repository;
import com.manthan.rentloop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // We will need this for login/JWT in Phase 3
    Optional<User> findByEmail(String email);
}