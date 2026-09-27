package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.model.Listing;
import com.manthan.rentloop.model.RentalRequest;
import com.manthan.rentloop.model.RequestMatch;
import com.manthan.rentloop.repository.ListingRepository;
import com.manthan.rentloop.repository.RentalRequestRepository;
import com.manthan.rentloop.repository.RequestMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RentalRequestRepository rentalRequestRepository;
    private final ListingRepository listingRepository;
    private final RequestMatchRepository requestMatchRepository;
    private final ListingService listingService;

    /**
     * Finds listings matching a rental request's criteria and persists them as RequestMatch records.
     * Only the creator of the RentalRequest is authorized to trigger this.
     *
     * @param requestId the ID of the rental request
     * @param userEmail the email of the authenticated user (must match the requester)
     * @return matched listings as DTOs
     */
    @Transactional
    public List<ListingResponse> findAndSaveMatchesForRequest(Long requestId, String userEmail) {
        RentalRequest rentalRequest = rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Rental request not found"));

        // OWNERSHIP AUTHORIZATION CHECK
        if (!rentalRequest.getRequester().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to find matches for this request.");
        }

        // Query for listings matching all criteria
        List<Listing> matchedListings = listingRepository.findMatchingListings(
                rentalRequest.getCategory().getId(),
                rentalRequest.getBudgetPerDay(),
                rentalRequest.getStartDate(),
                rentalRequest.getEndDate()
        );

        // Persist new matches (skip any already-persisted duplicates)
        List<Listing> newlyMatched = new ArrayList<>();
        for (Listing listing : matchedListings) {
            boolean alreadyMatched = requestMatchRepository
                    .existsByRentalRequestIdAndListingId(requestId, listing.getId());
            if (!alreadyMatched) {
                RequestMatch match = new RequestMatch();
                match.setRentalRequest(rentalRequest);
                match.setListing(listing);
                requestMatchRepository.save(match);
            }
            newlyMatched.add(listing);
        }

        // Return all current matches as DTOs (both existing and new)
        return newlyMatched.stream()
                .map(listingService::mapToResponse)
                .collect(Collectors.toList());
    }
}
