package com.personalfin.server.split.repository;

import com.personalfin.server.split.model.ExpenseShare;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, UUID> {

    List<ExpenseShare> findByGroupId(UUID groupId);

    List<ExpenseShare> findByGroupIdAndMemberName(UUID groupId, String memberName);

    @Query("SELECT es.memberName, SUM(es.amount) as total FROM ExpenseShare es WHERE es.group.id = :groupId GROUP BY es.memberName")
    List<MemberBalanceProjection> getMemberBalances(@Param("groupId") UUID groupId);

    interface MemberBalanceProjection {
        String getMemberName();
        java.math.BigDecimal getTotal();
    }
}







