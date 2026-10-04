package com.manthan.rentloop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.rentloop.dto.ItemRequest;
import com.manthan.rentloop.dto.ItemResponse;
import com.manthan.rentloop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    @WithMockUser(username = "owner@example.com")
    void createItem_Authenticated_Returns201() throws Exception {
        ItemRequest request = new ItemRequest();
        request.setCategoryId(10L);
        request.setName("DSLR Camera");
        request.setDescription("Canon EOS");
        request.setCondition("GOOD");

        ItemResponse response = new ItemResponse();
        response.setId(100L);
        response.setName("DSLR Camera");

        when(itemService.createItem(eq("owner@example.com"), any(ItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("DSLR Camera"));
    }
}
