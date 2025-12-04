package com.personalfin.server.security.service;

import com.personalfin.server.security.model.AuditLog;
import com.personalfin.server.security.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logSecurityEvent(String eventType, String userId, String username, String ipAddress, 
                                 String details, boolean success) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventType(eventType);
        auditLog.setUserId(userId != null ? UUID.fromString(userId) : null);
        auditLog.setUsername(username);
        auditLog.setIpAddress(ipAddress);
        auditLog.setDetails(details);
        auditLog.setSuccess(success);
        auditLog.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logAuthentication(String username, String ipAddress, boolean success, String reason) {
        logSecurityEvent("AUTHENTICATION", null, username, ipAddress, 
                        success ? "Login successful" : "Login failed: " + reason, success);
    }

    @Transactional
    public void logRegistration(String username, String ipAddress, boolean success, String reason) {
        logSecurityEvent("REGISTRATION", null, username, ipAddress,
                        success ? "Registration successful" : "Registration failed: " + reason, success);
    }

    @Transactional
    public void logSensitiveOperation(String eventType, UUID userId, String username, String ipAddress, 
                                      String details) {
        logSecurityEvent(eventType, userId.toString(), username, ipAddress, details, true);
    }

    @Transactional
    public void logDataAccess(String eventType, UUID userId, String username, String ipAddress, 
                             String resourceType, String resourceId) {
        logSecurityEvent(eventType, userId.toString(), username, ipAddress,
                        String.format("Accessed %s with ID: %s", resourceType, resourceId), true);
    }
}

