package com.fullStack.expenseTracker.specifications;

import com.fullStack.expenseTracker.models.Transaction;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(
            String email,
            LocalDate from,
            LocalDate to,
            Integer categoryId,
            Double minAmount,
            Double maxAmount) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (email != null && !email.isEmpty()) {
                predicates.add(cb.equal(root.join("user", JoinType.INNER).get("email"), email));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.join("category", JoinType.INNER).get("categoryId"), categoryId));
            }

            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

