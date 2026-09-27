package com.manthan.rentloop.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ItemRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String condition; // NEW, GOOD, etc.
    private List<ItemAttributeDto> attributes;
}
