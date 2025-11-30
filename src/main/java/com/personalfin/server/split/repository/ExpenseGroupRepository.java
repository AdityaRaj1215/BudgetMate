package com.personalfin.server.split.repository;

import com.personalfin.server.split.model.ExpenseGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, UUID> {

    List<ExpenseGroup> findByActiveTrueOrderByCreatedAtDesc();
}







