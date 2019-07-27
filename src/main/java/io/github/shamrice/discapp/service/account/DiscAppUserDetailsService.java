package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DiscAppUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(DiscAppUserDetailsService.class);

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

    public DiscAppUser getByUsername(String username) {
        return discappUserRepository.findByUsername(username);
    }

    public Long getOwnerIdForUsername(String username) {
        DiscAppUser user = discappUserRepository.findByUsername(username);
        if (user != null) {
            return user.getOwnerId();
        }
        else return null;
    }

    public boolean saveDiscAppUser(DiscAppUser user) {

        //TODO : add logging
        if (user != null) {

            try {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    String plainPassword = user.getPassword();
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(15);
                    String encodedPassword = passwordEncoder.encode(plainPassword);
                    user.setPassword(encodedPassword);
                }

                DiscAppUser createdUser = discappUserRepository.save(user);
                if (createdUser != null && createdUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                    return true;
                }
            } catch (Exception ex) {
                logger.error("Failed to save disc app user: " + ex.getMessage(), ex);
            }
        }

        return false;
    }
}
