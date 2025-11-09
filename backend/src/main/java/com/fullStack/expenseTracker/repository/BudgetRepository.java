package com.fullStack.expenseTracker.repository;

import com.fullStack.expenseTracker.models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Budget findByUserIdAndMonthAndYear(long userId, int month, long year);
    
    @Query(value = "SELECT * FROM budget WHERE user_id = :userId", nativeQuery = true)
    List<Budget> findByUserId(@Param("userId") long userId);
}
