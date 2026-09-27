package com.manthan.rentloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private BigDecimal pricePerDay;
    @NotNull
    private BigDecimal depositAmount;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;

    // Cloudinary URLs passed from frontend
    private List<String> imageUrls;
}