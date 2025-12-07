package com.personalfin.server.security.repository;

import com.personalfin.server.security.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, java.util.UUID> {

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username " +
           "AND la.successful = false " +
           "AND la.createdAt >= :since")
    long countFailedAttemptsSince(@Param("username") String username, @Param("since") OffsetDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress " +
           "AND la.successful = false " +
           "AND la.createdAt >= :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") OffsetDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username " +
           "ORDER BY la.createdAt DESC")
    List<LoginAttempt> findRecentAttemptsByUsername(@Param("username") String username);

    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress " +
           "ORDER BY la.createdAt DESC")
    List<LoginAttempt> findRecentAttemptsByIp(@Param("ipAddress") String ipAddress);
}


