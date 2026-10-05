package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only REST controller. Every endpoint requires ROLE_ADMIN.
 * Unauthenticated → 401, authenticated non-admin → 403.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ─── USER MANAGEMENT ──────────────────────────────────────────────────────

    /** List all registered users. */
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** View a specific user's details by ID. */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // ─── LISTING MODERATION ───────────────────────────────────────────────────

    /** View ALL listings regardless of status. */
    @GetMapping("/listings")
    public ResponseEntity<List<ListingResponse>> getAllListings() {
        return ResponseEntity.ok(adminService.getAllListings());
    }

    /** Force-remove (soft-delete) any listing as admin, bypassing ownership check. */
    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> adminRemoveListing(@PathVariable Long id) {
        adminService.adminRemoveListing(id);
        return ResponseEntity.noContent().build();
    }

    // ─── BOOKING OVERSIGHT ───────────────────────────────────────────────────

    /** View all bookings across the platform. */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    // ─── RENTAL REQUEST OVERSIGHT ─────────────────────────────────────────────

    /** View all rental requests across the platform. */
    @GetMapping("/rental-requests")
    public ResponseEntity<List<RentalRequestResponse>> getAllRentalRequests() {
        return ResponseEntity.ok(adminService.getAllRentalRequests());
    }

    // ─── REVIEW MODERATION ───────────────────────────────────────────────────

    /** View all reviews across the platform. */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(adminService.getAllReviews());
    }

    /** Hard-delete an inappropriate review. */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> adminDeleteReview(@PathVariable Long id) {
        adminService.adminDeleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
