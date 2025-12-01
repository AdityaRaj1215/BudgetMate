package com.personalfin.server.preferences.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.preferences.dto.UserPreferencesRequest;
import com.personalfin.server.preferences.dto.UserPreferencesResponse;
import com.personalfin.server.preferences.model.UserPreferences;
import com.personalfin.server.preferences.repository.UserPreferencesRepository;
import com.personalfin.server.user.service.UserService;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserService userService;

    public UserPreferencesService(UserPreferencesRepository preferencesRepository, UserService userService) {
        this.preferencesRepository = preferencesRepository;
        this.userService = userService;
    }

    @Transactional
    public UserPreferencesResponse getOrCreate() {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        return preferencesRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    UserPreferences preferences = new UserPreferences();
                    preferences.setUserId(userId);
                    preferences.setTheme("light");
                    preferences.setCurrency("INR");
                    return toResponse(preferencesRepository.save(preferences));
                });
    }

    @Transactional
    public UserPreferencesResponse update(UserPreferencesRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences newPrefs = new UserPreferences();
                    newPrefs.setUserId(userId);
                    return preferencesRepository.save(newPrefs);
                });

        if (request.theme() != null) {
            preferences.setTheme(request.theme());
        }
        if (request.currency() != null) {
            preferences.setCurrency(request.currency());
        }

        return toResponse(preferencesRepository.save(preferences));
    }

    private UserPreferencesResponse toResponse(UserPreferences preferences) {
        return new UserPreferencesResponse(
                preferences.getId(),
                preferences.getUserId().toString(),
                preferences.getTheme(),
                preferences.getCurrency(),
                preferences.getCreatedAt(),
                preferences.getUpdatedAt()
        );
    }
}







