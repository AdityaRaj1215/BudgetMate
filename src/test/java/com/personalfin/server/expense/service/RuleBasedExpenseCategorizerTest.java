package com.personalfin.server.expense.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.personalfin.server.expense.config.ExpenseCategorizerProperties;
import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleBasedExpenseCategorizerTest {

    private ExpenseCategorizer categorizer;

    @BeforeEach
    void setUp() {
        ExpenseCategorizerProperties props = new ExpenseCategorizerProperties();
        props.setDefaultCategory("Uncategorized");
        props.setCategories(Map.of(
                "Food", List.of("swiggy", "zomato"),
                "Travel", List.of("uber", "ola")
        ));
        categorizer = new RuleBasedExpenseCategorizer(props);
    }

    @Test
    void shouldReturnMatchingCategoryWhenKeywordFound() {
        ExpenseCategorizationResponse response = categorizer.categorize(
                new ExpenseCategorizationRequest("Dinner from Swiggy", "Swiggy", BigDecimal.valueOf(450)));

        assertThat(response.category()).isEqualTo("Food");
        assertThat(response.matchedKeyword()).isEqualTo("swiggy");
    }

    @Test
    void shouldReturnDefaultCategoryWhenNoMatch() {
        ExpenseCategorizationResponse response = categorizer.categorize(
                new ExpenseCategorizationRequest("Stationery purchase", "Office Store", BigDecimal.valueOf(300)));

        assertThat(response.category()).isEqualTo("Uncategorized");
        assertThat(response.matchedKeyword()).isNull();
    }
}

