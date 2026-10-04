package com.manthan.rentloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageDto {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    private String senderEmail;
    private String receiverEmail;

    @NotBlank(message = "Chat message content cannot be blank")
    @Size(max = 5000, message = "Chat message cannot exceed 5000 characters")
    private String content;
}

