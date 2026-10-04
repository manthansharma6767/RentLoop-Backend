package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserReviewsResponse;
import com.manthan.rentloop.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @WithMockUser(username = "renter@example.com")
    void submitReview_Authenticated_Returns201() throws Exception {
        ReviewDto request = ReviewDto.builder()
                .bookingId(500L)
                .rating(5)
                .comment("Great rental!")
                .build();

        ReviewDto response = ReviewDto.builder()
                .id(1L)
                .bookingId(500L)
                .reviewerEmail("renter@example.com")
                .targetEmail("owner@example.com")
                .rating(5)
                .comment("Great rental!")
                .build();

        when(reviewService.saveReview(any(ReviewDto.class), eq("renter@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @WithMockUser
    void getReviewsForUser_Returns200() throws Exception {
        UserReviewsResponse response = UserReviewsResponse.builder()
                .targetEmail("owner@example.com")
                .averageRating(4.8)
                .reviews(List.of())
                .build();

        when(reviewService.getReviewsForUser("owner@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/reviews/user/owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.8));
    }
}
