package com.personalfin.server.expense.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import com.personalfin.server.expense.service.ExpenseCategorizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = ExpenseCategorizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseCategorizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseCategorizer expenseCategorizer;

    @Test
    void shouldReturnCategoryResponse() throws Exception {
        when(expenseCategorizer.categorize(any(ExpenseCategorizationRequest.class)))
                .thenReturn(new ExpenseCategorizationResponse("Food", 0.9, "swiggy", Map.of("Food", 0.9)));

        ExpenseCategorizationRequest request =
                new ExpenseCategorizationRequest("Swiggy order", "Swiggy", BigDecimal.valueOf(650));

        mockMvc.perform(post("/api/expenses/categorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.matchedKeyword").value("swiggy"));
    }
}

