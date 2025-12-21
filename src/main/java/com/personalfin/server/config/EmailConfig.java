package com.personalfin.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        // Log configuration status (without exposing password)
        boolean credentialsSet = username != null && !username.isEmpty() 
                && password != null && !password.isEmpty();
        
        if (emailEnabled && !credentialsSet) {
            logger.warn("⚠️ Email is enabled but credentials are not set!");
            logger.warn("⚠️ Set EMAIL_USERNAME and EMAIL_PASSWORD environment variables");
            logger.warn("⚠️ Or edit application-dev.yml directly");
        } else if (emailEnabled && credentialsSet) {
            logger.info("✅ Email configuration loaded: host={}, port={}, username={}", 
                    host, port, username);
        } else {
            logger.info("ℹ️ Email is disabled (EMAIL_ENABLED=false)");
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}

