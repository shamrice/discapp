package io.github.shamrice.discapp.web.util;

import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.data.model.UserPermission;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.application.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountHelper {

    private static final String ANONYMOUS_USER = "anonymousUser";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscAppUserDetailsService userDetailsService;

    public boolean checkUserHasPermission(long appId, String permissionRequired) {
        if (permissionRequired == null || permissionRequired.trim().isEmpty()) {
            return false;
        }
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

                    //check to see if there's app specific permissions for that user.
                    UserPermission userPermission = applicationService.getApplicationPermissionsForUser(appId, principal.getId());
                    if (userPermission != null) {
                        return userPermission.getUserPermissions().contains(permissionRequired);
                    }
                }
            }

            //check default app permissions for logged in vs non logged in users.
            if (isLoggedIn && isUserAccount) {
                return applicationPermission.getRegisteredUserPermissions().contains(permissionRequired);
            } else {
                return applicationPermission.getUnregisteredUserPermissions().contains(permissionRequired);
            }
        }

        //if permissions don't exist and checking for hold permissions, return false so threads do not need approval.
        if (io.github.shamrice.discapp.service.application.permission.UserPermission.HOLD.equalsIgnoreCase(permissionRequired)) {
            log.warn("User permissions are null when attempting to check if user has hold permissions. Defaulting permission to false.");
            return false;
        }

        //return false that user has NONE permission if permissions are not set.
        //default to true if app permissions aren't set for other permissions.
        return !permissionRequired.equalsIgnoreCase(io.github.shamrice.discapp.service.application.permission.UserPermission.NONE);
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

    public DiscAppUserPrincipal getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER)) {
                DiscAppUserPrincipal principal = (DiscAppUserPrincipal) auth.getPrincipal();
                return principal;
            }
        }
        return null;
    }

    public Long getLoggedInDiscAppUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.isAuthenticated() && !auth.getPrincipal().equals(ANONYMOUS_USER)) {
                DiscAppUserPrincipal principal = (DiscAppUserPrincipal) auth.getPrincipal();
                return principal.getId();
            }
        }
        return null;
    }

    public boolean isRootAdminAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof DiscAppUserPrincipal) {
            DiscAppUserPrincipal userPrincipal = (DiscAppUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userPrincipal != null) {
                return userPrincipal.isRoot();
            }
        }
        return false;
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
