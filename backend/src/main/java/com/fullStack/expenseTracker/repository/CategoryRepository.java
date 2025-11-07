package com.fullStack.expenseTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.TransactionType;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByCategoryNameAndTransactionType(String categoryName, TransactionType transactionType);

}
