package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


@Service
public class DiscAppUserDetailsService implements UserDetailsService {

    @Autowired
    private DiscAppUserRepository discappUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        DiscAppUser user = discappUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        //principal is returned back and verified by "spring magic"
        return new DiscAppUserPrincipal(user);
    }

    public Long getOwnerIdForUsername(String username) {
        DiscAppUser user = discappUserRepository.findByUsername(username);
        if (user != null) {
            return user.getOwnerId();
        }
        else return null;
    }
}
