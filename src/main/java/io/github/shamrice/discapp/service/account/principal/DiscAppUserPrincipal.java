package io.github.shamrice.discapp.service.account.principal;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.UserPermission;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ToString
@Slf4j
public class DiscAppUserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_ADMIN = ROLE_PREFIX + "ADMIN";
    private static final String ROLE_USER = ROLE_PREFIX + "USER";
    private static final String ROLE_EDITOR = ROLE_PREFIX + "EDITOR";
    private static final String ROLE_SYSTEM = ROLE_PREFIX + "SYSTEM";
    private static final String ROLE_ROOT = ROLE_PREFIX + "ROOT";

    private DiscAppUser user;
    private boolean isRoot;

    private ApplicationService applicationService;

    public DiscAppUserPrincipal(DiscAppUser user, boolean isRoot, ApplicationService applicationService) {
        this.user = user;
        this.isRoot = isRoot;
        this.applicationService = applicationService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        if (user.getIsAdmin()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_ADMIN));
            //admins are editors... maintenance filter decides what page they can view.
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_EDITOR));
        }

        if (user.getIsUserAccount()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_USER));
        } else {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_SYSTEM));
        }

        //check if user has any editor permissions on any apps. If so, give them the editor role.
        List<UserPermission> userPermissions = applicationService.getAllApplicationPermissionsForUser(user.getId());
        if (userPermissions != null && userPermissions.size() > 0) {
            for (UserPermission permission : userPermissions) {
                if (permission != null && permission.getUserPermissions() != null && !permission.getUserPermissions().isEmpty()) {
                    if (permission.getUserPermissions().contains(io.github.shamrice.discapp.service.application.permission.UserPermission.EDIT)) {
                        grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_EDITOR));
                        log.info("Logged in user id: " + user.getId() + " editor permissions found in appId: "
                                + permission.getApplicationId() + ". Granting ROLE_EDITOR.");
                        break;
                    }
                }
            }
        }

        //give root access to configured account. Don't store email address in db in case db gets compromised.
        if (this.isRoot) {
            log.warn("Granting userId: " + user.getId() + " :: ROOT ACCESS.");
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_ROOT));
        }

        return grantedAuthorityList;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }

    public Long getOwnerId() {
        return user.getOwnerId();
    }

    public Boolean isAdmin() {
        return user.getIsAdmin();
    }

    public Boolean isUserAccount() {
        return user.getIsUserAccount();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public long getId() {
        return user.getId();
    }

    public boolean isRoot() {
        return isRoot;
    }

    public Integer getPasswordFailCount() {
        return user.getPasswordFailCount();
    }

    public boolean isAccountLocked() {
        if (user.getLockedUntilDate() == null) {
            return false;
        }
        return (user.getLockedUntilDate().getTime() > (new Date().getTime()));
    }
}
