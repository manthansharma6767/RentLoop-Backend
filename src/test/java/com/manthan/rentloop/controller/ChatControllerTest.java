package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    @WithMockUser(username = "renter@example.com")
    void getChatHistory_AuthorizedParticipant_Returns200() throws Exception {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(1L);
        response.setBookingId(500L);
        response.setSenderEmail("renter@example.com");
        response.setContent("Hello!");

        when(chatService.getChatHistory(500L, "renter@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/chat/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello!"));
    }

    @Test
    @WithMockUser(username = "stranger@example.com")
    void getChatHistory_UnrelatedUser_Returns403() throws Exception {
        when(chatService.getChatHistory(500L, "stranger@example.com"))
                .thenThrow(new AccessDeniedException("You are not authorized to view the chat history for this booking."));

        mockMvc.perform(get("/api/chat/500"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to view the chat history for this booking."));
    }
}
