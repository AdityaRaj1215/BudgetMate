package com.personalfin.server.savings.repository;

import com.personalfin.server.savings.model.SavingsGoal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    List<SavingsGoal> findByActiveTrueOrderByCreatedAtDesc();
}










