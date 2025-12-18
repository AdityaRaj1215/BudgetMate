package com.personalfin.server.expense.repository;

import com.personalfin.server.expense.model.Expense;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ExpenseSpecifications {

    public static Specification<Expense> filterByUserAndCriteria(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            String category,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String search,
            String paymentMethod) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter by user
            predicates.add(cb.equal(root.get("userId"), userId));
            
            // Date range filters
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            
            // Category filter
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            
            // Amount range filters
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            
            // Search text (searches in description and merchant)
            if (search != null && !search.isBlank()) {
                String searchLower = "%" + search.toLowerCase() + "%";
                Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), searchLower);
                Predicate merchantMatch = cb.like(cb.lower(root.get("merchant")), searchLower);
                predicates.add(cb.or(descriptionMatch, merchantMatch));
            }
            
            // Payment method filter
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("paymentMethod")), paymentMethod.toLowerCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}




