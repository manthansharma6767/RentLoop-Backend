package com.manthan.rentloop.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatMessageResponse {

    private Long id;
    private Long bookingId;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private Instant timestamp;
}
