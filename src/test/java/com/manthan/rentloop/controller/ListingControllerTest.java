package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.ListingRequest;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.service.ListingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ListingService listingService;

    @Test
    @WithMockUser(username = "owner@example.com")
    void createListing_Authenticated_Returns201() throws Exception {
        ListingRequest request = new ListingRequest();
        request.setItemId(10L);
        request.setTitle("Canon EOS Camera");
        request.setPricePerDay(BigDecimal.valueOf(30.00));
        request.setDepositAmount(BigDecimal.valueOf(50.00));
        request.setLatitude(BigDecimal.valueOf(37.7749));
        request.setLongitude(BigDecimal.valueOf(-122.4194));

        ListingResponse response = new ListingResponse();
        response.setId(100L);
        response.setTitle("Canon EOS Camera");

        when(listingService.createListing(eq("owner@example.com"), any(ListingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Canon EOS Camera"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void searchListings_Authenticated_Returns200() throws Exception {
        ListingResponse response = new ListingResponse();
        response.setId(100L);
        response.setTitle("Canon EOS Camera");

        when(listingService.searchAvailableListings(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/listings/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Canon EOS Camera"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void deleteListing_Authenticated_Returns204() throws Exception {
        mockMvc.perform(delete("/api/listings/100"))
                .andExpect(status().isNoContent());
    }
}
