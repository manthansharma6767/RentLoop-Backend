package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ChatMessageDto;
import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.model.Booking;
import com.manthan.rentloop.model.ChatMessage;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;

    /**
     * Persist a chat message ensuring the sender is an authorized booking participant.
     */
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageDto dto, String authenticatedSenderEmail) {
        if (authenticatedSenderEmail == null || authenticatedSenderEmail.isBlank()) {
            throw new AccessDeniedException("User must be authenticated to send chat messages.");
        }

        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + dto.getBookingId()));

        String renterEmail = booking.getRenter().getEmail();
        String ownerEmail = booking.getListing().getItem().getOwner().getEmail();

        boolean isRenter = authenticatedSenderEmail.equals(renterEmail);
        boolean isOwner = authenticatedSenderEmail.equals(ownerEmail);

        if (!isRenter && !isOwner) {
            throw new AccessDeniedException("You are not authorized to send messages for this booking.");
        }

        String receiverEmail = isRenter ? ownerEmail : renterEmail;

        ChatMessage message = new ChatMessage();
        message.setBookingId(booking.getId());
        message.setSenderEmail(authenticatedSenderEmail);
        message.setReceiverEmail(receiverEmail);
        message.setContent(dto.getContent());

        ChatMessage saved = chatMessageRepository.save(message);
        return toResponse(saved);
    }

    /**
     * Fetch the full chat history for a given booking if authorized.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Long bookingId, String authenticatedUserEmail) {
        if (authenticatedUserEmail == null || authenticatedUserEmail.isBlank()) {
            throw new AccessDeniedException("Authentication required to view chat history.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        String renterEmail = booking.getRenter().getEmail();
        String ownerEmail = booking.getListing().getItem().getOwner().getEmail();

        if (!authenticatedUserEmail.equals(renterEmail) && !authenticatedUserEmail.equals(ownerEmail)) {
            throw new AccessDeniedException("You are not authorized to view the chat history for this booking.");
        }

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

