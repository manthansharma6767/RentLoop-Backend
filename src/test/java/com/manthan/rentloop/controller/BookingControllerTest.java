package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.BookingRequest;
import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "renter@example.com")
    void createBooking_Authenticated_Returns201() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setListingId(100L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        BookingResponse response = new BookingResponse();
        response.setId(500L);
        response.setStatus("REQUESTED");

        when(bookingService.createBooking(eq("renter@example.com"), any(BookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void createBooking_Unauthenticated_Returns401Or403() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setListingId(100L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void approveBooking_Owner_Returns200() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(500L);
        response.setStatus("CONFIRMED");

        when(bookingService.approveBooking("owner@example.com", 500L)).thenReturn(response);

        mockMvc.perform(patch("/api/bookings/500/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void startBooking_Owner_Returns200() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(500L);
        response.setStatus("ACTIVE");

        when(bookingService.startBooking("owner@example.com", 500L)).thenReturn(response);

        mockMvc.perform(patch("/api/bookings/500/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void returnBooking_Owner_Returns200() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(500L);
        response.setStatus("RETURNED");

        when(bookingService.returnBooking("owner@example.com", 500L)).thenReturn(response);

        mockMvc.perform(patch("/api/bookings/500/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }
}
