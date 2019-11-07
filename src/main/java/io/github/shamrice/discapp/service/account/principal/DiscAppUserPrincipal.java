package io.github.shamrice.discapp.service.account.principal;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.service.application.ApplicationService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
@Slf4j
public class DiscAppUserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_ADMIN = ROLE_PREFIX + "ADMIN";
    private static final String ROLE_USER = ROLE_PREFIX + "USER";
    private static final String ROLE_SYSTEM = ROLE_PREFIX + "SYSTEM";
    private static final String ROLE_ROOT = ROLE_PREFIX + "ROOT";

    //todo: this ain't working none.
  //  @Value("${discapp.rootemail}")
   // private String ROOT_ACCOUNT_EMAIL;

    private DiscAppUser user;

    public DiscAppUserPrincipal(DiscAppUser user) {
        this.user = user;
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
/*
        log.warn("Entered email = " + user.getEmail() + " :: root = " + ROOT_ACCOUNT_EMAIL);

        //todo : figure out a better way
        if (user.getEmail().equals(ROOT_ACCOUNT_EMAIL)) {
            log.warn("Granting user: " + user.toString() + " :: ROOT ACCESS.");
            grantedAuthorityList.add(new SimpleGrantedAuthority(ROLE_ROOT));
        }
*/
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
}
