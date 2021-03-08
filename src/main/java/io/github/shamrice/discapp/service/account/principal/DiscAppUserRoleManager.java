package io.github.shamrice.discapp.service.account.principal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static io.github.shamrice.discapp.service.account.principal.UserRoles.ROLE_ADMIN;

@Slf4j
public class DiscAppUserRoleManager {

    /**
     * When the first application is added to an account, this method is used to keep
     * the user from having to log out and log back in in order to refresh their
     * account roles so they can access their admin page.
     */
    public void addAdminRoleToCurrentLoggedInUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("Adding admin role to " + auth.getName() + " if does not already exist.");

        List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        Authentication updatedAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(updatedAuth);
    }
}
