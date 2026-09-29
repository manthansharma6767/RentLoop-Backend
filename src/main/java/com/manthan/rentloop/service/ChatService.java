package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ChatMessageDto;
import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.model.ChatMessage;
import com.manthan.rentloop.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Persist a chat message and return the saved response DTO.
     */
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageDto dto) {
        ChatMessage message = new ChatMessage();
        message.setBookingId(dto.getBookingId());
        message.setSenderEmail(dto.getSenderEmail());
        message.setReceiverEmail(dto.getReceiverEmail());
        message.setContent(dto.getContent());

        ChatMessage saved = chatMessageRepository.save(message);
        return toResponse(saved);
    }

    /**
     * Fetch the full chat history for a given booking, ordered by timestamp ascending.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Long bookingId) {
        return chatMessageRepository.findByBookingIdOrderByTimestampAsc(bookingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setBookingId(message.getBookingId());
        response.setSenderEmail(message.getSenderEmail());
        response.setReceiverEmail(message.getReceiverEmail());
        response.setContent(message.getContent());
        response.setTimestamp(message.getTimestamp());
        return response;
    }
}
