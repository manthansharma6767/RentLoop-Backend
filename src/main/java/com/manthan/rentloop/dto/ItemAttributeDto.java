package com.manthan.rentloop.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemAttributeDto {
    @NotBlank
    private String key;
    @NotBlank
    private String value;
}