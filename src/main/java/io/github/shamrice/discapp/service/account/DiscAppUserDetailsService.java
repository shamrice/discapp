package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class DiscAppUserDetailsService implements UserDetailsService {

    @Autowired
    private DiscAppUserRepository discappUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be blank");
        }

        //DiscAppUser user = discappUserRepository.findByUsername(username);
        DiscAppUser user = discappUserRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        //principal is returned back and verified by "spring magic"
        return new DiscAppUserPrincipal(user);
    }

    public DiscAppUser getByDiscAppUserId(long userId) {
        Optional<DiscAppUser> discAppUser = discappUserRepository.findById(userId);
        return discAppUser.orElse(null);
    }

    public DiscAppUser getByEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            return discappUserRepository.findByEmail(email);
        }
        return null;
    }

    public Long getOwnerIdForEmail(String email) {
        DiscAppUser user = discappUserRepository.findByEmail(email);
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

                user.setUsername(user.getUsername().trim());
                user.setEmail(user.getEmail().trim());

                DiscAppUser createdUser = discappUserRepository.save(user);
                if (createdUser != null && createdUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                    log.info("Saved disc app user: " + createdUser.getEmail() + " : username: "
                            + createdUser.getUsername());
                    return true;
                }
            } catch (Exception ex) {
                log.error("Failed to save disc app user: " + user.getEmail() + " :: " + ex.getMessage(), ex);
            }
        }

        return false;
    }
}
