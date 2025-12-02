package com.personalfin.server.investment.repository;

import com.personalfin.server.investment.model.Investment;
import com.personalfin.server.investment.model.InvestmentType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {

    List<Investment> findByActiveTrueOrderByStartDateDesc();

    List<Investment> findByTypeAndActiveTrue(InvestmentType type);

    List<Investment> findByMaturityDateBetweenAndActiveTrue(LocalDate start, LocalDate end);
}










