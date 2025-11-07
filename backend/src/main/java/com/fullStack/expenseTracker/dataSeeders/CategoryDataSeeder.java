package com.fullStack.expenseTracker.dataSeeders;

import com.fullStack.expenseTracker.enums.ETransactionType;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.TransactionType;
import com.fullStack.expenseTracker.repository.CategoryRepository;
import com.fullStack.expenseTracker.repository.TransactionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryDataSeeder {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @EventListener
    @Transactional
    public void LoadCategories(ContextRefreshedEvent event) {
        // Get transaction types
        TransactionType expenseType = transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_EXPENSE);
        TransactionType incomeType = transactionTypeRepository.findByTransactionTypeName(ETransactionType.TYPE_INCOME);

        if (expenseType == null || incomeType == null) {
            return; // Transaction types not loaded yet
        }

        // Expense categories
        List<String> expenseCategories = Arrays.asList(
                "Food",
                "Leisure",
                "Household",
                "Clothing",
                "Education",
                "Healthcare",
                "Transport",
                "Utilities",
                "Entertainment",
                "Other"
        );

        // Income categories
        List<String> incomeCategories = Arrays.asList(
                "Salary",
                "Sales",
                "Awards",
                "Interest",
                "Freelance",
                "Investment",
                "Other"
        );

        // Add expense categories
        for (String categoryName : expenseCategories) {
            if (!categoryRepository.existsByCategoryNameAndTransactionType(categoryName, expenseType)) {
                categoryRepository.save(new Category(categoryName, expenseType, true));
            }
        }

        // Add income categories
        for (String categoryName : incomeCategories) {
            if (!categoryRepository.existsByCategoryNameAndTransactionType(categoryName, incomeType)) {
                categoryRepository.save(new Category(categoryName, incomeType, true));
            }
        }
    }
}
