package com.personalfin.server.expense.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.dto.ExpenseHeatmapPoint;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.expense.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void shouldCreateExpense() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "Dinner",
                "Restaurant",
                BigDecimal.valueOf(500),
                LocalDate.of(2025, 1, 10),
                "Food",
                "UPI"
        );

        when(expenseService.createExpense(any(ExpenseCreateRequest.class)))
                .thenReturn(new ExpenseResponse(
                        UUID.randomUUID(),
                        "Dinner",
                        "Restaurant",
                        "Food",
                        BigDecimal.valueOf(500),
                        LocalDate.of(2025, 1, 10),
                        "UPI",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void shouldReturnHeatmap() throws Exception {
        when(expenseService.heatmap(any(), any()))
                .thenReturn(List.of(
                        new ExpenseHeatmapPoint(LocalDate.of(2025, 1, 1), BigDecimal.valueOf(400), 1)
                ));

        mockMvc.perform(get("/api/expenses/heatmap")
                        .param("start", "2025-01-01")
                        .param("end", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].level").value(1));
    }
}

