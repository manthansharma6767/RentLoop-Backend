package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByBookingIdOrderByTimestampAsc(Long bookingId);
}
