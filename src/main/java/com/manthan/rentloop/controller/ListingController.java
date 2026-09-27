package com.manthan.rentloop.controller;
import com.manthan.rentloop.dto.ListingRequest;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingService.createListing(userDetails.getUsername(), request));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<ListingResponse>> getMyListings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(listingService.getMyListings(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        listingService.deleteListing(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ListingResponse>> searchListings(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(listingService.searchAvailableListings(
                categoryId, minPrice, maxPrice, startDate, endDate, pageable
        ));
    }
}