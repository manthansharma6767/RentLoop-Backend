package com.manthan.rentloop.dto;
import lombok.Data;
import java.util.List;

@Data
public class ItemResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String condition;
    private List<ItemAttributeDto> attributes;
}
