package com.noisevisionsoftware.nutrilog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.nutrilog.security.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final Firestore firestore;
    private final Cache<String, String> userEmailCache;

    public String getUserEmail(String userId) {
        try {
            // Najpierw sprawdź cache
            String cachedEmail = userEmailCache.getIfPresent(userId);
            if (cachedEmail != null) {
                return cachedEmail;
            }

            // Jeśli nie ma w cache, pobierz z Firestore
            DocumentReference userRef = firestore.collection("users").document(userId);
            DocumentSnapshot document = userRef.get().get();

            if (document.exists()) {
                String email = document.getString("email");
                if (email != null) {
                    userEmailCache.put(userId, email);
                    return email;
                }
            }

            return "Nieznany użytkownik";
        } catch (Exception e) {
            log.error("Error fetching user email for userId: {}", userId, e);
            return "Nieznany użytkownik";
        }
    }

    public boolean existsById(String userId) {
        try {
            DocumentReference userRef = firestore.collection("users").document(userId);
            return userRef.get().get().exists();
        } catch (Exception e) {
            log.error("Error checking user existence for userId: {}", userId, e);
            return false;
        }
    }

    @Cacheable(value = "userRoles", key = "#userId")
    public UserRole getUserRole(String userId) {
        try {
            DocumentReference userRef = firestore.collection("users").document(userId);
            DocumentSnapshot document = userRef.get().get();

            if (document.exists()) {
                String roleStr = document.getString("role");
                if (roleStr != null) {
                    return UserRole.valueOf(roleStr);
                }
            }

            return UserRole.USER;
        } catch (Exception e) {
            log.error("Error fetching user role for userId: {}", userId, e);
            return UserRole.USER;
        }
    }

    public boolean isAdmin(String userId) {
        return UserRole.ADMIN.equals(getUserRole(userId));
    }
}