package com.manthan.rentloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalRequestDto {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal budgetPerDay;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;
}
