package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.RentalRequestDto;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.CategoryRepository;
import com.manthan.rentloop.repository.RentalRequestRepository;
import com.manthan.rentloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalRequestServiceTest {

    @Mock
    private RentalRequestRepository rentalRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private RentalRequestService rentalRequestService;

    private User requester;
    private Category category;
    private RentalRequest rentalRequest;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setEmail("requester@example.com");

        category = new Category();
        category.setId(10L);
        category.setName("Gaming");

        rentalRequest = new RentalRequest();
        rentalRequest.setId(200L);
        rentalRequest.setRequester(requester);
        rentalRequest.setCategory(category);
        rentalRequest.setDescription("Need PS5 for weekend");
        rentalRequest.setBudgetPerDay(BigDecimal.valueOf(15.00));
        rentalRequest.setStartDate(LocalDate.now().plusDays(1));
        rentalRequest.setEndDate(LocalDate.now().plusDays(3));
        rentalRequest.setStatus(RentalRequestStatus.PENDING);
    }

    @Test
    void createRentalRequest_Success() {
        RentalRequestDto dto = new RentalRequestDto();
        dto.setCategoryId(10L);
        dto.setDescription("Need PS5 for weekend");
        dto.setBudgetPerDay(BigDecimal.valueOf(15.00));
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findByEmail("requester@example.com")).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(rentalRequestRepository.save(any(RentalRequest.class))).thenAnswer(i -> {
            RentalRequest r = i.getArgument(0);
            r.setId(200L);
            return r;
        });

        RentalRequestResponse response = rentalRequestService.createRentalRequest("requester@example.com", dto);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("Need PS5 for weekend", response.getDescription());
    }

    @Test
    void createRentalRequest_InvalidDates_ThrowsException() {
        RentalRequestDto dto = new RentalRequestDto();
        dto.setCategoryId(10L);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(2)); // End before start

        when(userRepository.findByEmail("requester@example.com")).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class, () ->
                rentalRequestService.createRentalRequest("requester@example.com", dto)
        );
    }

    @Test
    void cancelRentalRequest_Success_ByRequester() {
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(rentalRequest));

        rentalRequestService.cancelRentalRequest("requester@example.com", 200L);

        assertEquals(RentalRequestStatus.CANCELLED, rentalRequest.getStatus());
        verify(rentalRequestRepository, times(1)).save(rentalRequest);
    }

    @Test
    void cancelRentalRequest_UnauthorizedUser_ThrowsException() {
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(rentalRequest));

        assertThrows(IllegalArgumentException.class, () ->
                rentalRequestService.cancelRentalRequest("stranger@example.com", 200L)
        );
    }

    @Test
    void cancelRentalRequest_AlreadyCancelled_ThrowsException() {
        rentalRequest.setStatus(RentalRequestStatus.CANCELLED);
        when(rentalRequestRepository.findById(200L)).thenReturn(Optional.of(rentalRequest));

        assertThrows(IllegalArgumentException.class, () ->
                rentalRequestService.cancelRentalRequest("requester@example.com", 200L)
        );
    }
}
