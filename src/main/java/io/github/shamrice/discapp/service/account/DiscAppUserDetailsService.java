package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscappUser;
import io.github.shamrice.discapp.data.repository.DiscappUserRepository;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DiscAppUserDetailsService implements UserDetailsService {

    @Autowired
    private DiscappUserRepository discappUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        DiscappUser user = discappUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new DiscAppUserPrincipal(user);
    }
}
