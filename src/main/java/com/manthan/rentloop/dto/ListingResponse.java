package com.manthan.rentloop.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingResponse {
    private Long id;
    private ItemResponse item;
    private String title;
    private BigDecimal pricePerDay;
    private BigDecimal depositAmount;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private List<String> imageUrls;
}