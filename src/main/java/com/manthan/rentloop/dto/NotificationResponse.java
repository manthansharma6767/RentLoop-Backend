package com.manthan.rentloop.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class NotificationResponse {

    private Long id;
    private String recipientEmail;
    private String message;
    private String type;
    private boolean read;
    private Instant timestamp;
}
