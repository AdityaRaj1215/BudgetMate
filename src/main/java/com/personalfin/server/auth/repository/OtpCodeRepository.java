package com.personalfin.server.auth.repository;

import com.personalfin.server.auth.model.OtpCode;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    @Query("""
            SELECT o FROM OtpCode o
            WHERE o.email = :email
              AND o.used = false
              AND o.expiresAt >= :now
            ORDER BY o.createdAt DESC
            """)
    Optional<OtpCode> findActiveByEmail(@Param("email") String email, @Param("now") OffsetDateTime now);

    @Query("""
            SELECT o FROM OtpCode o
            WHERE o.email = :email
            ORDER BY o.createdAt DESC
            """)
    Optional<OtpCode> findLatestByEmail(@Param("email") String email);
}

