package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.ItemAttributeDto;
import com.manthan.rentloop.dto.ItemRequest;
import com.manthan.rentloop.dto.ItemResponse;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.CategoryRepository;
import com.manthan.rentloop.repository.ItemRepository;
import com.manthan.rentloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private Category category;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        category = new Category();
        category.setId(10L);
        category.setName("Cameras");

        item = new Item();
        item.setId(100L);
        item.setOwner(owner);
        item.setCategory(category);
        item.setName("Sony Alpha A7 III");
        item.setDescription("Full-frame Mirrorless Camera");
        item.setCondition(ItemCondition.GOOD);
    }

    @Test
    void createItem_Success_WithAttributes() {
        ItemRequest request = new ItemRequest();
        request.setCategoryId(10L);
        request.setName("Sony Alpha A7 III");
        request.setDescription("Full-frame Mirrorless Camera");
        request.setCondition("GOOD");

        ItemAttributeDto attr = new ItemAttributeDto();
        attr.setKey("Sensor");
        attr.setValue("Full Frame");
        request.setAttributes(List.of(attr));

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> {
            Item saved = i.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ItemResponse response = itemService.createItem("owner@example.com", request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Sony Alpha A7 III", response.getName());
        assertEquals("Cameras", response.getCategoryName());
        assertEquals("GOOD", response.getCondition());
        assertEquals(1, response.getAttributes().size());
        assertEquals("Sensor", response.getAttributes().get(0).getKey());
    }

    @Test
    void createItem_UserNotFound_ThrowsException() {
        ItemRequest request = new ItemRequest();
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                itemService.createItem("unknown@example.com", request)
        );
    }

    @Test
    void createItem_CategoryNotFound_ThrowsException() {
        ItemRequest request = new ItemRequest();
        request.setCategoryId(99L);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                itemService.createItem("owner@example.com", request)
        );
    }
}
