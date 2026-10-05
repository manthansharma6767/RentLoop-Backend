package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    // ─── USER MANAGEMENT ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_AdminAccess_Returns200() throws Exception {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(1L);
        dto.setName("Alice");
        dto.setEmail("alice@example.com");
        dto.setRole("USER");

        when(adminService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_NormalUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_Unauthenticated_Returns401Or403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_AdminAccess_Returns200() throws Exception {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(2L);
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setRole("ADMIN");

        when(adminService.getUserById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound_Returns400() throws Exception {
        when(adminService.getUserById(999L))
                .thenThrow(new IllegalArgumentException("User not found with ID: 999"));

        mockMvc.perform(get("/api/admin/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found with ID: 999"));
    }

    // ─── LISTING MODERATION ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAllListings_AdminAccess_Returns200() throws Exception {
        ListingResponse listing = new ListingResponse();
        listing.setId(10L);
        listing.setTitle("Camera Rental");
        listing.setStatus("ACTIVE");

        when(adminService.getAllListings()).thenReturn(List.of(listing));

        mockMvc.perform(get("/api/admin/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Camera Rental"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAllListings_NormalUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/listings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRemoveListing_AdminAccess_Returns204() throws Exception {
        doNothing().when(adminService).adminRemoveListing(10L);

        mockMvc.perform(delete("/api/admin/listings/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminRemoveListing_NormalUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/listings/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRemoveListing_NotFound_Returns400() throws Exception {
        doThrow(new IllegalArgumentException("Listing not found with ID: 999"))
                .when(adminService).adminRemoveListing(999L);

        mockMvc.perform(delete("/api/admin/listings/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Listing not found with ID: 999"));
    }

    // ─── BOOKING OVERSIGHT ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAllBookings_AdminAccess_Returns200() throws Exception {
        BookingResponse booking = new BookingResponse();
        booking.setId(20L);
        booking.setListingTitle("Camera Rental");
        booking.setStatus("CONFIRMED");

        when(adminService.getAllBookings()).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAllBookings_NormalUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isForbidden());
    }

    // ─── RENTAL REQUEST OVERSIGHT ────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAllRentalRequests_AdminAccess_Returns200() throws Exception {
        RentalRequestResponse req = new RentalRequestResponse();
        req.setId(30L);
        req.setStatus("PENDING");

        when(adminService.getAllRentalRequests()).thenReturn(List.of(req));

        mockMvc.perform(get("/api/admin/rental-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAllRentalRequests_NormalUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/rental-requests"))
                .andExpect(status().isForbidden());
    }

    // ─── REVIEW MODERATION ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAllReviews_AdminAccess_Returns200() throws Exception {
        ReviewDto review = ReviewDto.builder()
                .id(40L)
                .reviewerEmail("renter@example.com")
                .targetEmail("owner@example.com")
                .rating(3)
                .build();

        when(adminService.getAllReviews()).thenReturn(List.of(review));

        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listAllReviews_NormalUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteReview_AdminAccess_Returns204() throws Exception {
        doNothing().when(adminService).adminDeleteReview(40L);

        mockMvc.perform(delete("/api/admin/reviews/40"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminDeleteReview_NormalUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/40"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteReview_NotFound_Returns400() throws Exception {
        doThrow(new IllegalArgumentException("Review not found with ID: 999"))
                .when(adminService).adminDeleteReview(999L);

        mockMvc.perform(delete("/api/admin/reviews/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Review not found with ID: 999"));
    }
}
