package com.manthan.rentloop.dto;

import lombok.Data;

@Data
public class ChatMessageDto {

    private Long bookingId;
    private String senderEmail;
    private String receiverEmail;
    private String content;
}
