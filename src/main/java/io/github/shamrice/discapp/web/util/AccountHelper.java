package io.github.shamrice.discapp.web.util;

import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.permission.UserPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccountHelper {

    private static final String ANONYMOUS_USER = "anonymousUser";

    @Autowired
    private ApplicationService applicationService;

    public boolean checkUserHasPermission(long appId, String permissionRequired) {

        ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);
        if (applicationPermission != null) {

            boolean isUserAccount = false;
            boolean isLoggedIn = false;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                if (auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER)) {
                    DiscAppUserPrincipal principal = (DiscAppUserPrincipal) auth.getPrincipal();
                    isUserAccount = principal.isUserAccount();
                    isLoggedIn = true;
                }
            }

            if (isLoggedIn && isUserAccount) {
                return applicationPermission.getRegisteredUserPermissions().contains(permissionRequired);
            } else {
                return applicationPermission.getUnregisteredUserPermissions().contains(permissionRequired);
            }
        }
        //return false that user has NONE permission if permissions are not set.
        //default to true if app permissions aren't set for other permissions.
        return !permissionRequired.equalsIgnoreCase(UserPermission.NONE);
    }

    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER);
        }

        return false;
    }

    public String getLoggedInEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER)) {
                DiscAppUserPrincipal principal = (DiscAppUserPrincipal) auth.getPrincipal();
                return principal.getEmail();
            }
        }
        return null;
    }

    /*
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

     */

}
