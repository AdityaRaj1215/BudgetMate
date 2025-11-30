package com.personalfin.server.auth.util;

import com.personalfin.server.user.service.UserService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    public static UUID getCurrentUserId(UserService userService) {
        String username = getCurrentUsername();
        if (username != null) {
            return userService.getUserByUsername(username).getId();
        }
        return null;
    }
}



