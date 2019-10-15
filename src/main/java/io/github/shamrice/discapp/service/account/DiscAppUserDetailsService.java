package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.account.notification.EmailNotificationService;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DiscAppUserDetailsService implements UserDetailsService {

    @Autowired
    private DiscAppUserRepository discappUserRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private final static String NEW_ACCOUNT_EMAIL = "NEW_ACCOUNT_EMAIL";

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

    public List<DiscAppUser> searchByUsername(String searchTerm, boolean searchUserAccounts) {
        return discappUserRepository.findByUsernameContainingIgnoreCaseAndIsUserAccount(searchTerm, searchUserAccounts);
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

    public List<DiscAppUser> getByOwnerId(long ownerId) {
        return discappUserRepository.findByOwnerId(ownerId);
    }

    public Long getOwnerIdForEmail(String email) {
        DiscAppUser user = discappUserRepository.findByEmail(email);
        if (user != null) {
            return user.getOwnerId();
        }
        else return null;
    }

    public boolean saveDiscAppUser(DiscAppUser user) {
        return saveDiscAppUser(user, true);
    }

    public boolean saveDiscAppUser(DiscAppUser user, boolean sendNewUserEmailNotification) {

        if (user != null) {

            boolean isNewUser = false;
            if (user.getId() == null || user.getId() <= 0) {
                isNewUser = true;
            }

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

                    //send email notification
                    if (isNewUser && sendNewUserEmailNotification) {
                        sendNewUserEmailNotification(createdUser.getEmail());
                    }

                    return true;
                }
            } catch (Exception ex) {
                log.error("Failed to save disc app user: " + user.getEmail() + " :: " + ex.getMessage(), ex);
            }
        }

        return false;
    }

    public boolean updateDiscAppUser(long userId, String username, boolean isShowEmail) {
        log.info("Updating user id: " + userId + " with username: " + username + " : showEmail: " + isShowEmail);
        return discappUserRepository.updateDiscAppUser(userId, username, isShowEmail, new Date()) > 0;
    }

    public boolean updateOwnerInformation(long userId, long ownerId, boolean isAdmin) {
        log.info("Updating user owner info: userId: " + userId + " : ownerId: " + ownerId + " : isAdmin: " + isAdmin);
        return discappUserRepository.updateDiscAppUserOwnerInfo(userId, ownerId, isAdmin, new Date()) > 0;
    }

    public boolean updateDiscAppUserEnabled(long userId, boolean isEnabled) {
        log.info("Updating user enabled status: userId: " + userId + " : isEnabled: " + isEnabled);
        return discappUserRepository.updateDiscAppUserEnabled(userId, isEnabled, new Date()) > 0;
    }

    private void sendNewUserEmailNotification(String newUserEmail) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put(NEW_ACCOUNT_EMAIL, newUserEmail);

        String adminEmail = configurationService.getStringValue(0L, ConfigurationProperty.EMAIL_ADMIN_ADDRESS, null);
        if (adminEmail == null) {
            log.error("Could not find admin email address in configuration property: "
                    + ConfigurationProperty.EMAIL_ADMIN_ADDRESS.getPropName() + " : new account email notification not sent.");
            return;
        }

        emailNotificationService.send(adminEmail, NotificationType.NEW_ACCOUNT_CREATED, templateParams);
    }
}
