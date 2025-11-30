package com.personalfin.server.preferences.web;

import com.personalfin.server.preferences.dto.UserPreferencesRequest;
import com.personalfin.server.preferences.dto.UserPreferencesResponse;
import com.personalfin.server.preferences.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    public UserPreferencesController(UserPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserPreferencesResponse> getPreferences(@PathVariable String userId) {
        return ResponseEntity.ok(preferencesService.getOrCreate(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @PathVariable String userId,
            @Valid @RequestBody UserPreferencesRequest request) {
        return ResponseEntity.ok(preferencesService.update(userId, request));
    }
}







