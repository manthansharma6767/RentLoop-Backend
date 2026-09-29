package com.manthan.rentloop.controller;

import com.manthan.rentloop.dto.ChatMessageDto;
import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * STOMP message handler: receives a chat message from the sender,
     * persists it, and pushes it to the receiver's private queue.
     *
     * Client sends to: /app/chat.send
     * Receiver subscribes to: /user/{receiverEmail}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        ChatMessageResponse saved = chatService.saveMessage(chatMessageDto);

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
     * REST endpoint to fetch the full chat history for a specific booking.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long bookingId) {
        return ResponseEntity.ok(chatService.getChatHistory(bookingId));
    }
}
