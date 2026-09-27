package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.dto.RentalRequestDto;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.service.MatchingService;
import com.manthan.rentloop.service.RentalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-requests")
@RequiredArgsConstructor
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;
    private final MatchingService matchingService;

    @PostMapping
    public ResponseEntity<RentalRequestResponse> createRentalRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RentalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalRequestService.createRentalRequest(userDetails.getUsername(), request));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<RentalRequestResponse>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(rentalRequestService.getMyRequests(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalRequestResponse> getRentalRequestById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(rentalRequestService.getRentalRequestById(userDetails.getUsername(), id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRentalRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        rentalRequestService.cancelRentalRequest(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{requestId}/matches")
    public ResponseEntity<List<ListingResponse>> findMatchesForRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        List<ListingResponse> matches = matchingService
                .findAndSaveMatchesForRequest(requestId, userDetails.getUsername());
        return ResponseEntity.ok(matches);
    }
}
