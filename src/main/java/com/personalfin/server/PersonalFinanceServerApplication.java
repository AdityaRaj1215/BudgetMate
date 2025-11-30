package com.personalfin.server;

import com.personalfin.server.auth.config.JwtProperties;
import com.personalfin.server.budget.config.BudgetProperties;
import com.personalfin.server.expense.config.ExpenseAnalyticsProperties;
import com.personalfin.server.expense.config.ExpenseCategorizerProperties;
import com.personalfin.server.reminder.config.ReminderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        ExpenseCategorizerProperties.class,
        ExpenseAnalyticsProperties.class,
        ReminderProperties.class,
        BudgetProperties.class,
        JwtProperties.class,
        com.personalfin.server.config.RateLimitingConfig.class
})
public class PersonalFinanceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceServerApplication.class, args);
    }
}
