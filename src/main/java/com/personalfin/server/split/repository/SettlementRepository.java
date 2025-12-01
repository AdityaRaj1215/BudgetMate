package com.personalfin.server.split.repository;

import com.personalfin.server.split.model.Settlement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByGroupId(UUID groupId);

    List<Settlement> findByGroupIdAndSettledFalse(UUID groupId);
}









