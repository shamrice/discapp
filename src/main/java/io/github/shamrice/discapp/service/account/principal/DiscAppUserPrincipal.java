package io.github.shamrice.discapp.service.account.principal;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscAppUserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private DiscAppUser user;

    public DiscAppUserPrincipal(DiscAppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        if (user.getAdmin()) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_PREFIX + "ADMIN"));
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
        return user.getAdmin();
    }

    public String getEmail() {
        return user.getEmail();
    }
}
