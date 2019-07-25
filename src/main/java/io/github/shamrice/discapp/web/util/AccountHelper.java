package io.github.shamrice.discapp.web.util;

import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AccountHelper {

    private final static String ANONYMOUS_USER = "anonymousUser";

    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER);
        }

        return false;
    }

    public String getLoggedInUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER)) {
                DiscAppUserPrincipal principal = (DiscAppUserPrincipal) auth.getPrincipal();
                return principal.getUsername();
            }
        }
        return null;
    }

}
