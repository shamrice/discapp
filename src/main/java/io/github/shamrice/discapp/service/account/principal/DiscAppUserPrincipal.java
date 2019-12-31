package io.github.shamrice.discapp.service.account.principal;

import io.github.shamrice.discapp.data.model.DiscAppUser;
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
    private static final String ROLE_SYSTEM = ROLE_PREFIX + "SYSTEM";
    private static final String ROLE_ROOT = ROLE_PREFIX + "ROOT";

    private DiscAppUser user;
    private boolean isRoot;

    public DiscAppUserPrincipal(DiscAppUser user, boolean isRoot) {
        this.user = user;
        this.isRoot = isRoot;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        if (user.getIsAdmin()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        }

        if (user.getIsUserAccount()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_USER));
        } else {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_SYSTEM));
        }

        //give root access to configured account. Don't store email address in db in case db gets compromised.
        if (this.isRoot) {
            log.warn("Granting user: " + user.toString() + " :: ROOT ACCESS.");
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
