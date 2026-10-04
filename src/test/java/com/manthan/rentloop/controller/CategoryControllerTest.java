package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.CategoryDto;
import com.manthan.rentloop.dto.CategoryRequest;
import com.manthan.rentloop.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_PublicAccess_Returns200() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Books");

        when(categoryService.getAllCategories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Books"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_AdminUser_Returns201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("College Textbooks");

        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Books");

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_NormalUser_Returns403() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_Unauthenticated_Returns401Or403() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_AdminUser_Returns204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}
