package com.personalfin.server.preferences.web;

import com.personalfin.server.preferences.dto.UserPreferencesRequest;
import com.personalfin.server.preferences.dto.UserPreferencesResponse;
import com.personalfin.server.preferences.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getPreferences() {
        return ResponseEntity.ok(preferencesService.getOrCreate());
    }

    @PutMapping
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @Valid @RequestBody UserPreferencesRequest request) {
        return ResponseEntity.ok(preferencesService.update(request));
    }
}







