package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.ListingRepository;
import com.manthan.rentloop.repository.RentalRequestRepository;
import com.manthan.rentloop.repository.RequestMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private RentalRequestRepository rentalRequestRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private RequestMatchRepository requestMatchRepository;

    @Mock
    private ListingService listingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MatchingService matchingService;

    private User requester;
    private Category category;
    private RentalRequest request;
    private Listing matchedListing;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setEmail("requester@example.com");

        category = new Category();
        category.setId(10L);
        category.setName("Laptops");

        request = new RentalRequest();
        request.setId(200L);
        request.setRequester(requester);
        request.setCategory(category);
        request.setBudgetPerDay(BigDecimal.valueOf(20.00));
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        matchedListing = new Listing();
        matchedListing.setId(300L);
        matchedListing.setPricePerDay(BigDecimal.valueOf(15.00));
    }

    @Test
    void findAndSaveMatchesForRequest_MatchesFound_SavesMatchAndNotifiesRequester() {
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(listingRepository.findMatchingListings(10L, BigDecimal.valueOf(20.00), request.getStartDate(), request.getEndDate()))
                .thenReturn(List.of(matchedListing));
        when(requestMatchRepository.existsByRentalRequestIdAndListingId(200L, 300L)).thenReturn(false);

        ListingResponse responseDto = new ListingResponse();
        responseDto.setId(300L);
        when(listingService.mapToResponse(matchedListing)).thenReturn(responseDto);

        List<ListingResponse> matches = matchingService.findAndSaveMatchesForRequest(200L, "requester@example.com");

        assertEquals(1, matches.size());
        assertEquals(300L, matches.get(0).getId());

        verify(requestMatchRepository, times(1)).save(any(RequestMatch.class));
        verify(notificationService, times(1)).createAndSend(eq("requester@example.com"), anyString(), eq(NotificationType.MATCH_FOUND));
    }

    @Test
    void findAndSaveMatchesForRequest_NoMatches_NoNotificationSent() {
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(listingRepository.findMatchingListings(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        List<ListingResponse> matches = matchingService.findAndSaveMatchesForRequest(200L, "requester@example.com");

        assertTrue(matches.isEmpty());
        verify(notificationService, never()).createAndSend(anyString(), anyString(), any());
    }

    @Test
    void findAndSaveMatchesForRequest_UnauthorizedUser_ThrowsException() {
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () ->
                matchingService.findAndSaveMatchesForRequest(200L, "stranger@example.com")
        );
    }
}
