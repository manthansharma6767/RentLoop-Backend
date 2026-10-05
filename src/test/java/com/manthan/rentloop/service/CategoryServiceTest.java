package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.CategoryDto;
import com.manthan.rentloop.dto.CategoryRequest;
import com.manthan.rentloop.model.Category;
import com.manthan.rentloop.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Laptops, Cameras, Gadgets");
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Laptops, Cameras, Gadgets");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto created = categoryService.createCategory(request);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Electronics", created.getName());
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDto> categories = categoryService.getAllCategories();

        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                categoryService.deleteCategory(99L)
        );

        assertTrue(ex.getMessage().contains("Category not found"));
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics Updated");
        request.setDescription("New description");

        Category updated = new Category();
        updated.setId(1L);
        updated.setName("Electronics Updated");
        updated.setDescription("New description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        assertEquals("Electronics Updated", result.getName());
        assertEquals("New description", result.getDescription());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CategoryRequest request = new CategoryRequest();
        request.setName("Some Name");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                categoryService.updateCategory(99L, request)
        );

        assertTrue(ex.getMessage().contains("Category not found with ID: 99"));
        verify(categoryRepository, never()).save(any());
    }
}
