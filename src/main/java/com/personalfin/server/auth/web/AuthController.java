package com.personalfin.server.auth.web;

import com.personalfin.server.auth.dto.LoginRequest;
import com.personalfin.server.auth.dto.LoginResponse;
import com.personalfin.server.auth.dto.RegisterRequest;
import com.personalfin.server.auth.service.JwtTokenService;
import com.personalfin.server.user.model.User;
import com.personalfin.server.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserDetailsService userDetailsService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LoginResponse.of(token, user.getUsername(), user.getEmail(), roles));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);

        User user = userService.getUserByUsername(userDetails.getUsername());
        List<String> roles = user.getRoles().stream()
                .map(role -> role.name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(LoginResponse.of(token, user.getUsername(), user.getEmail(), roles));
    }
}

