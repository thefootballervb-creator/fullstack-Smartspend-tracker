package com.fullStack.expenseTracker.controllers;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.CategoryRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.CategoryAlreadyExistsException;
import com.fullStack.expenseTracker.exceptions.CategoryNotFoundException;
import com.fullStack.expenseTracker.services.CategoryService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDto categoryRequestDto;
    private ApiResponseDto<?> successResponse;

    /**
     * Sets up test data before each test method.
     * This method is automatically invoked by JUnit's @BeforeEach lifecycle hook.
     */
    @BeforeEach
    public void setUp() {
        categoryRequestDto = new CategoryRequestDto("Test Category", 1);
        successResponse = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.CREATED,
                "Category created successfully"
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllCategories_Success() throws Exception {
        // Arrange
        ApiResponseDto<?> response = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                new Object()
        );

        when(categoryService.getCategories())
                .thenReturn(ResponseEntity.ok(response));

        // Act & Assert
        mockMvc.perform(get("/mywallet/category/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddNewCategory_Success() throws Exception {
        // Arrange
        when(categoryService.addNewCategory(any(CategoryRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(successResponse));

        // Act & Assert
        mockMvc.perform(post("/mywallet/category/new")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(categoryRequestDto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddNewCategory_AlreadyExists() throws Exception {
        // Arrange
        when(categoryService.addNewCategory(any(CategoryRequestDto.class)))
                .thenThrow(new CategoryAlreadyExistsException("Category already exists"));

        // Act & Assert
        mockMvc.perform(post("/mywallet/category/new")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(categoryRequestDto))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_Success() throws Exception {
        // Arrange
        int categoryId = 1;
        ApiResponseDto<?> updateResponse = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                "Category updated successfully"
        );

        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDto.class)))
                .thenReturn(ResponseEntity.ok(updateResponse));

        // Act & Assert
        mockMvc.perform(put("/mywallet/category/update")
                        .with(Objects.requireNonNull(csrf()))
                        .param("categoryId", String.valueOf(categoryId))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(categoryRequestDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_NotFound() throws Exception {
        // Arrange
        int categoryId = 999;
        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDto.class)))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        // Act & Assert
        mockMvc.perform(put("/mywallet/category/update")
                        .with(Objects.requireNonNull(csrf()))
                        .param("categoryId", String.valueOf(categoryId))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(categoryRequestDto))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCategory_Success() throws Exception {
        // Arrange
        int categoryId = 1;
        ApiResponseDto<?> deleteResponse = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                "Category deleted successfully"
        );

        when(categoryService.enableOrDisableCategory(categoryId))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        // Act & Assert
                        mockMvc.perform(delete("/mywallet/category/delete")
                                .with(Objects.requireNonNull(csrf()))
                                .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddNewCategory_InvalidInput() throws Exception {
        // Arrange
        CategoryRequestDto invalidRequest = new CategoryRequestDto("", 1);

        // Act & Assert
                        mockMvc.perform(post("/mywallet/category/new")
                                .with(Objects.requireNonNull(csrf()))
                                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                                .content(Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest))))
                .andExpect(status().isBadRequest());
    }
}

