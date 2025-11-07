package com.fullStack.expenseTracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.fullStack.expenseTracker.dto.reponses.PageResponseDto;
import com.fullStack.expenseTracker.dto.reponses.TransactionResponseDto;
import com.fullStack.expenseTracker.dto.requests.TransactionRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.TransactionNotFoundException;
import com.fullStack.expenseTracker.services.TransactionService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionRequestDto transactionRequestDto;
    private ApiResponseDto<?> successResponse;

    /**
     * Sets up test data before each test method.
     * This method is automatically invoked by JUnit's @BeforeEach lifecycle hook.
     */
    /**
     * Sets up test data before each test method.
     * This method is automatically invoked by JUnit's @BeforeEach lifecycle hook.
     */
    @BeforeEach
    void setUp() {
        transactionRequestDto = new TransactionRequestDto(
                "test@example.com",
                1,
                "Test transaction",
                100.0,
                LocalDate.now()
        );

        successResponse = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                "Transaction created successfully"
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddTransaction_Success() throws Exception {
        // Arrange
        when(transactionService.addTransaction(any(TransactionRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(successResponse));

        // Act & Assert
        mockMvc.perform(post("/mywallet/transaction/new")
                        .with(Objects.requireNonNull(csrf()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(transactionRequestDto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionById_Success() throws Exception {
        // Arrange
        Long transactionId = 1L;
        TransactionResponseDto transactionResponse = new TransactionResponseDto(
                1L, 1, "Category1", 1, "Description", 100.0, LocalDate.now(), "test@example.com"
        );
        ApiResponseDto<TransactionResponseDto> response = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                transactionResponse
        );

        when(transactionService.getTransactionById(transactionId))
                .thenReturn(ResponseEntity.ok(response));

        // Act & Assert
        mockMvc.perform(get("/mywallet/transaction/getById")
                        .param("id", transactionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transactionId").value(1L))
                .andExpect(jsonPath("$.data.description").value("Description"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionById_NotFound() throws Exception {
        // Arrange
        Long transactionId = 999L;
        when(transactionService.getTransactionById(transactionId))
                .thenThrow(new TransactionNotFoundException("Transaction not found"));

        // Act & Assert
        mockMvc.perform(get("/mywallet/transaction/getById")
                        .param("id", transactionId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateTransaction_Success() throws Exception {
        // Arrange
        long transactionId = 1L;
        when(transactionService.updateTransaction(eq(transactionId), any(TransactionRequestDto.class)))
                .thenReturn(ResponseEntity.ok(successResponse));

        // Act & Assert
        mockMvc.perform(put("/mywallet/transaction/update")
                        .with(Objects.requireNonNull(csrf()))
                        .param("transactionId", String.valueOf(transactionId))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(transactionRequestDto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteTransaction_Success() throws Exception {
        // Arrange
        Long transactionId = 1L;
        ApiResponseDto<?> deleteResponse = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                "Transaction deleted successfully"
        );

        when(transactionService.deleteTransaction(transactionId))
                .thenReturn(ResponseEntity.ok(deleteResponse));

        // Act & Assert
        mockMvc.perform(delete("/mywallet/transaction/delete")
                        .with(Objects.requireNonNull(csrf()))
                        .param("transactionId", String.valueOf(transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllTransactions_Success() throws Exception {
        // Arrange
        List<TransactionResponseDto> transactions = new ArrayList<>();
        transactions.add(new TransactionResponseDto(1L, 1, "Category1", 1, "Desc1", 100.0, LocalDate.now(), "test@example.com"));
        
        PageResponseDto<List<TransactionResponseDto>> pageResponse = new PageResponseDto<>(
                transactions, 1, 1L
        );

        ApiResponseDto<PageResponseDto<List<TransactionResponseDto>>> response = new ApiResponseDto<>(
                ApiResponseStatus.SUCCESS,
                HttpStatus.OK,
                pageResponse
        );

        when(transactionService.getAllTransactions(anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(response));

        // Act & Assert
        mockMvc.perform(get("/mywallet/transaction/getAll")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("searchKey", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.data").isArray());
    }
}

