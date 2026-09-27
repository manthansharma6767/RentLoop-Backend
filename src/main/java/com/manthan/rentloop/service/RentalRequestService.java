package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.RentalRequestDto;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.model.Category;
import com.manthan.rentloop.model.RentalRequest;
import com.manthan.rentloop.model.RentalRequestStatus;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.CategoryRepository;
import com.manthan.rentloop.repository.RentalRequestRepository;
import com.manthan.rentloop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalRequestService {

    private final RentalRequestRepository rentalRequestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public RentalRequestResponse createRentalRequest(String userEmail, RentalRequestDto dto) {
        User requester = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        RentalRequest request = new RentalRequest();
        request.setRequester(requester);
        request.setCategory(category);
        request.setDescription(dto.getDescription());
        request.setBudgetPerDay(dto.getBudgetPerDay());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setLatitude(dto.getLatitude());
        request.setLongitude(dto.getLongitude());

        RentalRequest saved = rentalRequestRepository.save(request);
        return mapToResponse(saved);
    }

    public List<RentalRequestResponse> getMyRequests(String userEmail) {
        return rentalRequestRepository.findByRequesterEmail(userEmail)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void cancelRentalRequest(String userEmail, Long requestId) {
        RentalRequest request = rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Rental request not found"));

        // OWNERSHIP AUTHORIZATION CHECK
        if (!request.getRequester().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to cancel this request.");
        }

        if (request.getStatus() == RentalRequestStatus.CANCELLED) {
            throw new IllegalArgumentException("This request is already cancelled.");
        }

        request.setStatus(RentalRequestStatus.CANCELLED);
        rentalRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public RentalRequestResponse getRentalRequestById(String userEmail, Long requestId) {
        RentalRequest request = rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Rental request not found"));

        // OWNERSHIP AUTHORIZATION CHECK
        if (!request.getRequester().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to view this request.");
        }

        return mapToResponse(request);
    }

    private RentalRequestResponse mapToResponse(RentalRequest request) {
        RentalRequestResponse response = new RentalRequestResponse();
        response.setId(request.getId());
        response.setRequesterName(request.getRequester().getName());
        response.setCategoryName(request.getCategory().getName());
        response.setDescription(request.getDescription());
        response.setBudgetPerDay(request.getBudgetPerDay());
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());
        response.setStatus(request.getStatus().name());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }
}
