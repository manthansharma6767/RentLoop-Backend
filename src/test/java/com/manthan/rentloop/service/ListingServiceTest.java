package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ListingRequest;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.ItemRepository;
import com.manthan.rentloop.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ListingService listingService;

    private User owner;
    private Item item;
    private Listing listing;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setName("Camera");

        listing = new Listing();
        listing.setId(100L);
        listing.setItem(item);
        listing.setTitle("Professional Camera Listing");
        listing.setPricePerDay(BigDecimal.valueOf(30.00));
        listing.setDepositAmount(BigDecimal.valueOf(100.00));
        listing.setStatus(ListingStatus.ACTIVE);
    }

    @Test
    void createListing_Success_ByItemOwner() {
        ListingRequest request = new ListingRequest();
        request.setItemId(10L);
        request.setTitle("Professional Camera Listing");
        request.setPricePerDay(BigDecimal.valueOf(30.00));
        request.setDepositAmount(BigDecimal.valueOf(100.00));
        request.setImageUrls(List.of("http://example.com/cam1.jpg"));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(listingRepository.save(any(Listing.class))).thenAnswer(i -> {
            Listing l = i.getArgument(0);
            l.setId(100L);
            return l;
        });

        ListingResponse response = listingService.createListing("owner@example.com", request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Professional Camera Listing", response.getTitle());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    void createListing_UnauthorizedNonOwner_ThrowsException() {
        ListingRequest request = new ListingRequest();
        request.setItemId(10L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                listingService.createListing("stranger@example.com", request)
        );

        assertTrue(ex.getMessage().contains("item you own"));
        verify(listingRepository, never()).save(any());
    }

    @Test
    void deleteListing_Success_ByOwner_PerformsSoftDelete() {
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        listingService.deleteListing("owner@example.com", 100L);

        assertEquals(ListingStatus.REMOVED, listing.getStatus());
        verify(listingRepository, times(1)).save(listing);
    }

    @Test
    void deleteListing_UnauthorizedNonOwner_ThrowsException() {
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                listingService.deleteListing("stranger@example.com", 100L)
        );

        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    void searchAvailableListings_ValidDates_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        Page<Listing> page = new PageImpl<>(List.of(listing));
        when(listingRepository.searchListings(null, null, null, start, end, pageable)).thenReturn(page);

        Page<ListingResponse> result = listingService.searchAvailableListings(null, null, null, start, end, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchAvailableListings_OnlyStartDateProvided_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
                listingService.searchAvailableListings(null, null, null, start, null, pageable)
        );
    }
}
