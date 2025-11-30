package com.personalfin.server.preferences.service;

import com.personalfin.server.preferences.dto.UserPreferencesRequest;
import com.personalfin.server.preferences.dto.UserPreferencesResponse;
import com.personalfin.server.preferences.model.UserPreferences;
import com.personalfin.server.preferences.repository.UserPreferencesRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;

    public UserPreferencesService(UserPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    @Transactional
    public UserPreferencesResponse getOrCreate(String userId) {
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
    public UserPreferencesResponse update(String userId, UserPreferencesRequest request) {
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

        return toResponse(preferences);
    }

    private UserPreferencesResponse toResponse(UserPreferences preferences) {
        return new UserPreferencesResponse(
                preferences.getId(),
                preferences.getUserId(),
                preferences.getTheme(),
                preferences.getCurrency(),
                preferences.getCreatedAt(),
                preferences.getUpdatedAt()
        );
    }
}







