package com.fullStack.expenseTracker.dto.reponses;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    private Long transactionId;

    private int categoryId;

    private String categoryName;

    private int transactionType;

    private String description;

    private double amount;

    private LocalDate date;

    private String userEmail;

}
