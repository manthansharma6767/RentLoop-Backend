package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.NotificationResponse;
import com.manthan.rentloop.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "user@example.com")
    void getNotifications_Returns200() throws Exception {
        NotificationResponse response = new NotificationResponse();
        response.setId(1L);
        response.setMessage("New booking request");

        when(notificationService.getNotifications("user@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("New booking request"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getUnreadCount_Returns200() throws Exception {
        when(notificationService.getUnreadCount("user@example.com")).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void markAsRead_Returns200() throws Exception {
        NotificationResponse response = new NotificationResponse();
        response.setId(1L);
        response.setRead(true);

        when(notificationService.markAsRead(1L, "user@example.com")).thenReturn(response);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void markAllAsRead_Returns204() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all"))
                .andExpect(status().isNoContent());
    }
}
