package com.fullStack.expenseTracker.services;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.TransactionRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.CategoryNotFoundException;
import com.fullStack.expenseTracker.exceptions.TransactionNotFoundException;
import com.fullStack.expenseTracker.exceptions.TransactionServiceLogicException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.Transaction;
import com.fullStack.expenseTracker.models.TransactionType;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.TransactionRepository;
import com.fullStack.expenseTracker.services.impls.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequestDto transactionRequestDto;
    private User user;
    private Category category;
    private Transaction transaction;

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

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();

        TransactionType transactionType = new TransactionType();
        transactionType.setTransactionTypeId(1);
        transactionType.setTransactionTypeName(com.fullStack.expenseTracker.enums.ETransactionType.TYPE_EXPENSE);

        category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Test Category");
        category.setTransactionType(transactionType);
        category.setEnabled(true);

        transaction = new Transaction();
        transaction.setTransactionId(1L);
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setDescription("Test transaction");
        transaction.setAmount(100.0);
        transaction.setDate(LocalDate.now());
    }

    @Test
    void testAddTransaction_Success() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(Objects.requireNonNull(user));
        when(categoryService.getCategoryById(1)).thenReturn(Objects.requireNonNull(category));
        @SuppressWarnings("null")
        Transaction nonNullTransaction = Objects.requireNonNull(transaction);
        @SuppressWarnings("null")
        var saveStub = when(transactionRepository.save(any(Transaction.class)));
        saveStub.thenReturn(nonNullTransaction);

        // Act
        ResponseEntity<ApiResponseDto<?>> response = transactionService.addTransaction(transactionRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiResponseStatus.SUCCESS, Objects.requireNonNull(response.getBody()).getStatus());
        @SuppressWarnings({"null", "unused"})
        var unused = verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testAddTransaction_UserNotFound() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            transactionService.addTransaction(transactionRequestDto);
        });
        assertNotNull(exception);
        @SuppressWarnings({"null", "unused"})
        var unused = verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testAddTransaction_CategoryNotFound() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(Objects.requireNonNull(user));
        when(categoryService.getCategoryById(1))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        // Act & Assert
        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> {
            transactionService.addTransaction(transactionRequestDto);
        });
        assertNotNull(exception);
        @SuppressWarnings({"null", "unused"})
        var unused = verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testAddTransaction_DataAccessException() throws Exception {
        // Arrange
        when(userService.findByEmail("test@example.com")).thenReturn(Objects.requireNonNull(user));
        when(categoryService.getCategoryById(1)).thenReturn(Objects.requireNonNull(category));
        @SuppressWarnings("null")
        var saveStub = when(transactionRepository.save(any(Transaction.class)));
        saveStub.thenThrow(new org.springframework.dao.DataAccessException("Database error") {});

        // Act & Assert
        TransactionServiceLogicException exception = assertThrows(TransactionServiceLogicException.class, () -> {
            transactionService.addTransaction(transactionRequestDto);
        });
        assertNotNull(exception);
    }

    @Test
    void testGetTransactionById_Success() throws Exception {
        // Arrange
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(Objects.requireNonNull(transaction)));

        // Act
        ResponseEntity<ApiResponseDto<?>> response = transactionService.getTransactionById(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiResponseStatus.SUCCESS, Objects.requireNonNull(response.getBody()).getStatus());
    }

    @Test
    void testGetTransactionById_NotFound() {
        // Arrange
        Long transactionId = 999L;
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById(transactionId);
        });
        assertNotNull(exception);
    }

    @Test
    void testDeleteTransaction_Success() throws Exception {
        // Arrange
        Long transactionId = 1L;
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(transactionId);

        // Act
        ResponseEntity<ApiResponseDto<?>> response = transactionService.deleteTransaction(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiResponseStatus.SUCCESS, Objects.requireNonNull(response.getBody()).getStatus());
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }

    @Test
    void testDeleteTransaction_NotFound() {
        // Arrange
        Long transactionId = 999L;
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        // Act & Assert
        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteTransaction(transactionId);
        });
        assertNotNull(exception);
        verify(transactionRepository, never()).deleteById(anyLong());
    }
}
