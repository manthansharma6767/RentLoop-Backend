package com.manthan.rentloop.service;
import com.manthan.rentloop.dto.ListingRequest;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.model.Item;
import com.manthan.rentloop.model.Listing;
import com.manthan.rentloop.model.ListingImage;
import com.manthan.rentloop.model.ListingStatus;
import com.manthan.rentloop.repository.ItemRepository;
import com.manthan.rentloop.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    @Transactional
    public ListingResponse createListing(String userEmail, ListingRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // OWNERSHIP AUTHORIZATION CHECK
        if (!item.getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only create a listing for an item you own.");
        }

        Listing listing = new Listing();
        listing.setItem(item);
        listing.setTitle(request.getTitle());
        listing.setPricePerDay(request.getPricePerDay());
        listing.setDepositAmount(request.getDepositAmount());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            boolean isFirst = true;
            for (String url : request.getImageUrls()) {
                ListingImage image = new ListingImage();
                image.setImageUrl(url);
                image.setIsPrimary(isFirst); // Make the first image primary
                listing.addImage(image);
                isFirst = false;
            }
        }

        Listing savedListing = listingRepository.save(listing);
        return mapToResponse(savedListing);
    }

    public List<ListingResponse> getMyListings(String email) {
        return listingRepository.findByItemOwnerEmail(email)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteListing(String userEmail, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        // OWNERSHIP AUTHORIZATION CHECK
        if (!listing.getItem().getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to delete this listing.");
        }

        // Soft delete (Best practice for financial/rental systems)
        listing.setStatus(ListingStatus.REMOVED);
        listingRepository.save(listing);
    }

    private ListingResponse mapToResponse(Listing listing) {
        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setPricePerDay(listing.getPricePerDay());
        response.setDepositAmount(listing.getDepositAmount());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        response.setStatus(listing.getStatus().name());
        response.setItem(itemService.mapToResponse(listing.getItem()));

        if (listing.getImages() != null) {
            response.setImageUrls(listing.getImages().stream()
                    .map(ListingImage::getImageUrl).collect(Collectors.toList()));
        }
        return response;
    }

    public Page<ListingResponse> searchAvailableListings(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        // Validation: If one date is provided, both must be provided
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            throw new IllegalArgumentException("Both start date and end date must be provided for availability check.");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        Page<Listing> listingsPage = listingRepository.searchListings(
                categoryId, minPrice, maxPrice, startDate, endDate, pageable
        );

        // Map the Page of Entities to a Page of DTOs
        return listingsPage.map(this::mapToResponse);
    }
}
