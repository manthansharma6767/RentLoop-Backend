package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ChatMessageDto;
import com.manthan.rentloop.dto.ChatMessageResponse;
import com.manthan.rentloop.model.Booking;
import com.manthan.rentloop.model.ChatMessage;
import com.manthan.rentloop.model.Item;
import com.manthan.rentloop.model.Listing;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.BookingRepository;
import com.manthan.rentloop.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ChatService chatService;

    private User renter;
    private User owner;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        renter = new User();
        renter.setId(2L);
        renter.setEmail("renter@example.com");

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setItem(item);

        booking = new Booking();
        booking.setId(500L);
        booking.setListing(listing);
        booking.setRenter(renter);
    }

    @Test
    void saveMessage_Success_DerivesSenderAndReceiverServerSide() {
        ChatMessageDto inputDto = new ChatMessageDto();
        inputDto.setBookingId(500L);
        inputDto.setSenderEmail("hacker@example.com"); // Client-supplied sender ignored
        inputDto.setContent("Hello owner!");

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessageResponse response = chatService.saveMessage(inputDto, "renter@example.com");

        assertNotNull(response);
        assertEquals("renter@example.com", response.getSenderEmail());
        assertEquals("owner@example.com", response.getReceiverEmail());
        assertEquals("Hello owner!", response.getContent());
    }

    @Test
    void getChatHistory_Success_ForParticipant() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setBookingId(500L);
        msg.setSenderEmail("renter@example.com");
        msg.setReceiverEmail("owner@example.com");
        msg.setContent("Hi");

        when(chatMessageRepository.findByBookingIdOrderByTimestampAsc(500L)).thenReturn(List.of(msg));

        List<ChatMessageResponse> history = chatService.getChatHistory(500L, "renter@example.com");

        assertEquals(1, history.size());
        assertEquals("Hi", history.get(0).getContent());
    }

    @Test
    void getChatHistory_Fails_ForUnrelatedUser() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () ->
                chatService.getChatHistory(500L, "stranger@example.com")
        );
    }
}
