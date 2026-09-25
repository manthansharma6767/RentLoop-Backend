package com.manthan.rentloop.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Instant createdAt;
}