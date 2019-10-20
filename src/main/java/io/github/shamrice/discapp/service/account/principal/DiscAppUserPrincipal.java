package io.github.shamrice.discapp.service.account.principal;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.service.application.ApplicationService;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
public class DiscAppUserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private DiscAppUser user;

    public DiscAppUserPrincipal(DiscAppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        if (user.getIsAdmin()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_PREFIX + "ADMIN"));
        }

        if (user.getIsUserAccount()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_PREFIX + "USER"));
        } else {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_PREFIX + "SYSTEM"));
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
}
