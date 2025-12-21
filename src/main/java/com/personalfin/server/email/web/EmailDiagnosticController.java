package com.personalfin.server.email.web;

import com.personalfin.server.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email/diagnostic")
public class EmailDiagnosticController {

    private final EmailService emailService;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String emailHost;

    @Value("${spring.mail.port:587}")
    private int emailPort;

    @Value("${spring.mail.username:}")
    private String emailUsername;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${spring.mail.from:no-reply@personalfinance.com}")
    private String emailFrom;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public EmailDiagnosticController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("emailEnabled", emailEnabled);
        config.put("emailHost", emailHost);
        config.put("emailPort", emailPort);
        config.put("emailUsername", emailUsername != null && !emailUsername.isEmpty() ? "***CONFIGURED***" : "❌ NOT SET");
        config.put("emailPassword", emailPassword != null && !emailPassword.isEmpty() ? "***CONFIGURED***" : "❌ NOT SET");
        config.put("emailFrom", emailFrom);
        config.put("activeProfile", activeProfile);
        config.put("emailServiceEnabled", emailService.isEmailEnabled());
        
        // Check if credentials are properly set
        boolean credentialsSet = emailUsername != null && !emailUsername.isEmpty() 
                && emailPassword != null && !emailPassword.isEmpty()
                && !emailFrom.equals("no-reply@personalfinance.com");
        
        config.put("status", credentialsSet && emailEnabled ? "✅ READY" : "❌ NOT CONFIGURED");
        config.put("message", credentialsSet && emailEnabled 
                ? "Email is configured and ready to send" 
                : "Email is not properly configured. Check EMAIL_USERNAME, EMAIL_PASSWORD, and EMAIL_FROM environment variables.");
        
        return ResponseEntity.ok(config);
    }
}


