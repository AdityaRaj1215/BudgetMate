package com.personalfin.server.auth.web;

import com.personalfin.server.auth.dto.LoginRequest;
import com.personalfin.server.auth.dto.LoginResponse;
import com.personalfin.server.auth.dto.OtpRequest;
import com.personalfin.server.auth.dto.OtpResponse;
import com.personalfin.server.auth.dto.PasswordValidationRequest;
import com.personalfin.server.auth.dto.PasswordValidationResponse;
import com.personalfin.server.auth.dto.RegisterRequest;
import com.personalfin.server.auth.service.JwtTokenService;
import com.personalfin.server.auth.service.OtpService;
import com.personalfin.server.security.exception.AccountLockedException;
import com.personalfin.server.security.service.AccountLockoutService;
import com.personalfin.server.security.service.AuditLogService;
import com.personalfin.server.security.util.SecurityUtils;
import com.personalfin.server.security.validation.PasswordValidator;
import com.personalfin.server.user.model.User;
import com.personalfin.server.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final AccountLockoutService accountLockoutService;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final PasswordValidator passwordValidator;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserDetailsService userDetailsService,
            UserService userService,
            AccountLockoutService accountLockoutService,
            AuditLogService auditLogService,
            OtpService otpService,
            PasswordValidator passwordValidator) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.accountLockoutService = accountLockoutService;
        this.auditLogService = auditLogService;
        this.otpService = otpService;
        this.passwordValidator = passwordValidator;
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        HttpServletRequest httpRequest = getHttpServletRequest();
        String ipAddress = httpRequest != null ? SecurityUtils.getClientIpAddress(httpRequest) : "unknown";
        
        try {
            // Verify OTP before creating account
            otpService.verifyOtp(request.email(), request.otp());

            User user = userService.createUser(
                    request.username(),
                    request.email(),
                    request.password()
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtTokenService.generateToken(userDetails);

            List<String> roles = user.getRoles().stream()
                    .map(role -> role.name())
                    .collect(Collectors.toList());

            // Log successful registration
            auditLogService.logRegistration(user.getUsername(), ipAddress, true, null);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(LoginResponse.of(token, user.getUsername(), user.getEmail(), roles));
        } catch (Exception e) {
            // Log failed registration
            auditLogService.logRegistration(request.username(), ipAddress, false, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register/validate-password")
    public ResponseEntity<PasswordValidationResponse> validatePassword(
            @Valid @RequestBody PasswordValidationRequest request) {
        PasswordValidator.PasswordValidationResult result = passwordValidator.validate(request.password());
        
        if (result.isValid()) {
            return ResponseEntity.ok(PasswordValidationResponse.valid());
        } else {
            return ResponseEntity.ok(PasswordValidationResponse.invalid(result.getErrorMessage()));
        }
    }

    @PostMapping("/register/otp")
    public ResponseEntity<OtpResponse> requestOtp(@Valid @RequestBody OtpRequest request) {
        HttpServletRequest httpRequest = getHttpServletRequest();
        String ipAddress = httpRequest != null ? SecurityUtils.getClientIpAddress(httpRequest) : "unknown";

        try {
            // Check if email already exists
            if (userService.existsByEmail(request.email())) {
                auditLogService.logRegistration(request.email(), ipAddress, false, "Email already exists");
                throw new IllegalArgumentException("Email already registered");
            }

            OtpResponse response = otpService.generateOtp(request.email());
            auditLogService.logRegistration(request.email(), ipAddress, true, "OTP generated");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            auditLogService.logRegistration(request.email(), ipAddress, false, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        HttpServletRequest httpRequest = getHttpServletRequest();
        String ipAddress = httpRequest != null ? SecurityUtils.getClientIpAddress(httpRequest) : "unknown";
        String username = request.username();

        // Check if account is locked
        if (accountLockoutService.isAccountLocked(username)) {
            long remainingAttempts = accountLockoutService.getRemainingAttempts(username);
            int lockoutDuration = accountLockoutService.getLockoutDurationMinutes();
            accountLockoutService.recordFailedAttempt(username, ipAddress, "Account locked");
            auditLogService.logAuthentication(username, ipAddress, false, "Account locked");
            throw new AccountLockedException(
                    "Account is temporarily locked due to too many failed login attempts. Please try again later.",
                    remainingAttempts,
                    lockoutDuration
            );
        }

        // Check if IP is blocked
        if (accountLockoutService.isIpBlocked(ipAddress)) {
            auditLogService.logAuthentication(username, ipAddress, false, "IP blocked");
            throw new BadCredentialsException("Too many failed attempts from this IP address");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.password()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenService.generateToken(userDetails);

            User user = userService.getUserByUsername(userDetails.getUsername());
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.name())
                    .collect(Collectors.toList());

            // Record successful login
            accountLockoutService.recordSuccessfulAttempt(username, ipAddress);
            auditLogService.logAuthentication(username, ipAddress, true, null);

            return ResponseEntity.ok(LoginResponse.of(token, user.getUsername(), user.getEmail(), roles));
        } catch (BadCredentialsException e) {
            // Record failed login attempt
            accountLockoutService.recordFailedAttempt(username, ipAddress, "Invalid credentials");
            auditLogService.logAuthentication(username, ipAddress, false, "Invalid credentials");
            
            long remainingAttempts = accountLockoutService.getRemainingAttempts(username);
            if (remainingAttempts <= 0) {
                int lockoutDuration = accountLockoutService.getLockoutDurationMinutes();
                throw new AccountLockedException(
                        "Account has been locked due to too many failed login attempts. Please try again after " +
                        lockoutDuration + " minutes.",
                        0,
                        lockoutDuration
                );
            }
            throw e;
        }
    }
}

