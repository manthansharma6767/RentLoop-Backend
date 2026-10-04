package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.UpdateProfileRequest;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "john@example.com")
    void getProfile_Authenticated_Returns200() throws Exception {
        UserProfileDto profile = new UserProfileDto();
        profile.setId(1L);
        profile.setName("John Doe");
        profile.setEmail("john@example.com");

        when(userService.getUserProfile("john@example.com")).thenReturn(profile);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getProfile_Unauthenticated_Returns401Or403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void updateProfile_Authenticated_Returns200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("John Updated");
        request.setPhone("1234567890");

        UserProfileDto profile = new UserProfileDto();
        profile.setName("John Updated");
        profile.setPhone("1234567890");

        when(userService.updateProfile(eq("john@example.com"), any(UpdateProfileRequest.class))).thenReturn(profile);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }
}
