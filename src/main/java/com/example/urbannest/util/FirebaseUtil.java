package com.example.urbannest.util;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class FirebaseUtil {
    public static FirebaseToken getFirebaseToken() {
        return (FirebaseToken) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}
