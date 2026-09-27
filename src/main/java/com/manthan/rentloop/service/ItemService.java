package com.manthan.rentloop.service;
import com.manthan.rentloop.dto.ItemAttributeDto;
import com.manthan.rentloop.dto.ItemRequest;
import com.manthan.rentloop.dto.ItemResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.CategoryRepository;
import com.manthan.rentloop.repository.ItemRepository;
import com.manthan.rentloop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemResponse createItem(String userEmail, ItemRequest request) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCondition(ItemCondition.valueOf(request.getCondition().toUpperCase()));

        // Add dynamic attributes (EAV pattern)
        if (request.getAttributes() != null) {
            for (ItemAttributeDto attrDto : request.getAttributes()) {
                ItemAttribute attr = new ItemAttribute();
                attr.setAttributeKey(attrDto.getKey());
                attr.setAttributeValue(attrDto.getValue());
                item.addAttribute(attr); // Helper method syncs relationship
            }
        }

        Item savedItem = itemRepository.save(item);
        return mapToResponse(savedItem);
    }

    public ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setCategoryId(item.getCategory().getId());
        response.setCategoryName(item.getCategory().getName());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setCondition(item.getCondition().name());

        if (item.getAttributes() != null) {
            response.setAttributes(item.getAttributes().stream().map(attr -> {
                ItemAttributeDto dto = new ItemAttributeDto();
                dto.setKey(attr.getAttributeKey());
                dto.setValue(attr.getAttributeValue());
                return dto;
            }).collect(Collectors.toList()));
        }
        return response;
    }
}
