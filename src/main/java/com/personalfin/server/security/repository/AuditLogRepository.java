package com.personalfin.server.security.repository;

import com.personalfin.server.security.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT al FROM AuditLog al WHERE al.eventType = :eventType ORDER BY al.createdAt DESC")
    List<AuditLog> findByEventType(@Param("eventType") String eventType);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AuditLog> findSince(@Param("since") OffsetDateTime since);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.eventType = :eventType " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserIdAndEventType(@Param("userId") UUID userId, @Param("eventType") String eventType);
}

