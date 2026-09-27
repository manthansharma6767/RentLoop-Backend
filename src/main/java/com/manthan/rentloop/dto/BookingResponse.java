package com.manthan.rentloop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class BookingResponse {

    private Long id;
    private Long listingId;
    private String listingTitle;
    private String renterName;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private String status;
    private Instant createdAt;
}
