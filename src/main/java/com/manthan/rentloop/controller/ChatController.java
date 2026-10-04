package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.ChatMessageDto;
import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * STOMP message handler: receives a chat message from the sender,
     * derives authenticated sender email from Principal, persists message,
     * and pushes it to the receiver's and sender's private queues.
     *
     * Client sends to: /app/chat.send
     * Receiver subscribes to: /user/{receiverEmail}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendMessage(Principal principal, @Payload @Valid ChatMessageDto chatMessageDto) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("Unauthenticated WebSocket user session.");
        }

        String authenticatedSenderEmail = principal.getName();
        ChatMessageResponse saved = chatService.saveMessage(chatMessageDto, authenticatedSenderEmail);

        // Push to the specific receiver's private queue
        messagingTemplate.convertAndSendToUser(
                saved.getReceiverEmail(),
                "/queue/messages",
                saved
        );

        // Also push back to the sender so their UI updates in real-time
        messagingTemplate.convertAndSendToUser(
                saved.getSenderEmail(),
                "/queue/messages",
                saved
        );
    }

    /**
     * REST endpoint to fetch full chat history for a specific booking.
     * Authorized ONLY for renter or listing owner of that booking.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication required to access chat history");
        }
        return ResponseEntity.ok(chatService.getChatHistory(bookingId, userDetails.getUsername()));
    }
}

