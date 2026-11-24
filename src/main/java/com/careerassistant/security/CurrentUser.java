package com.careerassistant.security;

import com.careerassistant.common.ApiException;
import com.careerassistant.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}
    public static UserAccount require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserAccount user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
        return user;
    }
}
