package com.manthan.rentloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingRequest {
    @NotNull
    private Long itemId;
    @NotBlank
    private String title;
    @NotNull
    @Positive(message = "pricePerDay must be greater than zero")
    private BigDecimal pricePerDay;
    @NotNull
    @PositiveOrZero(message = "depositAmount must be greater than or equal to zero")
    private BigDecimal depositAmount;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;

    // Cloudinary URLs passed from frontend
    private List<String> imageUrls;
}