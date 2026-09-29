package com.mat.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityUtils
 *
 * Helper utility for accessing SecurityContext details of the authenticated user.
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Gets the username of the logged-in user.
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Gets the user ID of the logged-in user.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof Long) {
            return (Long) auth.getCredentials();
        }
        return null;
    }

    /**
     * Gets the role name (e.g., "ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER") without "ROLE_" prefix.
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if (role != null && role.startsWith("ROLE_")) {
                    return role.substring(5);
                }
            }
        }
        return null;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getCurrentUserRole());
    }

    public static boolean isBaseCommander() {
        return "BASE_COMMANDER".equalsIgnoreCase(getCurrentUserRole());
    }

    public static boolean isLogisticsOfficer() {
        return "LOGISTICS_OFFICER".equalsIgnoreCase(getCurrentUserRole());
    }
}
