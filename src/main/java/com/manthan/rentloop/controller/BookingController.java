package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.BookingRequest;
import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(userDetails.getUsername(), request));
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<List<BookingResponse>> getMyRentals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getMyRentals(userDetails.getUsername()));
    }

    @GetMapping("/my-items")
    public ResponseEntity<List<BookingResponse>> getBookingsOnMyItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getBookingsOnMyItems(userDetails.getUsername()));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approveBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveBooking(userDetails.getUsername(), id));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<BookingResponse> startBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.startBooking(userDetails.getUsername(), id));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<BookingResponse> returnBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.returnBooking(userDetails.getUsername(), id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(userDetails.getUsername(), id));
    }
}

