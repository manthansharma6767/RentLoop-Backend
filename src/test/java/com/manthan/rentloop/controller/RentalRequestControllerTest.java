package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.RentalRequestDto;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.service.RentalRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RentalRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RentalRequestService rentalRequestService;

    @Test
    @WithMockUser(username = "requester@example.com")
    void createRentalRequest_Authenticated_Returns201() throws Exception {
        RentalRequestDto dto = new RentalRequestDto();
        dto.setCategoryId(10L);
        dto.setDescription("Need PS5 Console");
        dto.setBudgetPerDay(BigDecimal.valueOf(15.00));
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(3));
        dto.setLatitude(BigDecimal.valueOf(37.7749));
        dto.setLongitude(BigDecimal.valueOf(-122.4194));

        RentalRequestResponse response = new RentalRequestResponse();
        response.setId(200L);
        response.setDescription("Need PS5 Console");

        when(rentalRequestService.createRentalRequest(eq("requester@example.com"), any(RentalRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/rental-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200L))
                .andExpect(jsonPath("$.description").value("Need PS5 Console"));
    }

    @Test
    @WithMockUser(username = "requester@example.com")
    void getMyRequests_Authenticated_Returns200() throws Exception {
        RentalRequestResponse response = new RentalRequestResponse();
        response.setId(200L);
        response.setDescription("Need PS5 Console");

        when(rentalRequestService.getMyRequests("requester@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/rental-requests/my-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200L));
    }

    @Test
    @WithMockUser(username = "requester@example.com")
    void cancelRentalRequest_Authenticated_Returns204() throws Exception {
        mockMvc.perform(patch("/api/rental-requests/200/cancel"))
                .andExpect(status().isNoContent());
    }
}
