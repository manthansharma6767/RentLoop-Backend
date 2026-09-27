package com.manthan.rentloop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class RentalRequestResponse {

    private Long id;
    private String requesterName;
    private String categoryName;
    private String description;
    private BigDecimal budgetPerDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Instant createdAt;
}
