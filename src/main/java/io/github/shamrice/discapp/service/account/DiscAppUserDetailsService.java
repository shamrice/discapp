package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.account.notification.EmailNotificationService;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.define.url.AccountUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Value("${discapp.root.email}")
    private String rootAccountEmail;

    private final static String NEW_ACCOUNT_EMAIL = "NEW_ACCOUNT_EMAIL";

    @Override
    public UserDetails loadUserByUsername(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be blank");
        }

        DiscAppUser user = discappUserRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        //check if user account is locked. if so, block log in
        if (user.getLockedUntilDate() != null && user.getLockedUntilDate().getTime() > new Date().getTime()) {
            log.warn("User: " + email + " attempted to log in but their account is locked until: " + user.getLockedUntilDate().toString());

            //reset password fail count back to zero on locked accounts if not already set.
            if (user.getPasswordFailCount() != null && user.getPasswordFailCount() > 0) {
                discappUserRepository.updateDiscAppUserPasswordFailCountAndLastPasswordFailDateById(user.getId(), 0, new Date());
            }

            throw new LockedException("User: " + email + " account is currently locked until: " + user.getLockedUntilDate());
        }

        //principal is returned back and verified by "spring magic"
        return new DiscAppUserPrincipal(user, rootAccountEmail);
    }

    public List<DiscAppUser> searchByUsername(String searchTerm, boolean searchUserAccounts) {
        return discappUserRepository.findByUsernameContainingIgnoreCaseAndIsUserAccount(searchTerm, searchUserAccounts);
    }

    public DiscAppUser getByUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            return discappUserRepository.findByUsername(username.trim());
        }
        return null;
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
        return null;
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
                user.setModDt(new Date());

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

    public boolean setLastLoginDateToNow(long userId) {
        try {
            log.info("Setting last login date to now for userId: " + userId);
            return discappUserRepository.updateDiscAppUserLastLoginDateAndPasswordFailCountById(userId, new Date(), 0) > 0;
        } catch (Exception ex) {
            log.error("Failed to set last log in date for userId: " + userId + " :: " + ex.getMessage(), ex);
        }
        return false;
    }

    public void incrementPasswordLastFailCount(String email, String baseUrl) {
        try {
            DiscAppUser user = discappUserRepository.findByEmail(email);
            if (user != null) {

                int currentFailCount = user.getPasswordFailCount() + 1;

                //if account is already locked, just return back as there is nothing to do
                if (user.getLockedUntilDate() != null && user.getLockedUntilDate().getTime() > new Date().getTime()) {
                    log.warn("Account: " + email + " is already locked until: + " + user.getLockedUntilDate());
                    return;
                }

                int maxFailuresBeforeLock = configurationService.getIntegerValue(
                        ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID,
                        ConfigurationProperty.MAX_LOGIN_ATTEMPTS_BEFORE_LOCK,
                        5);

                //lock account if they surpass max attempts.
                if (currentFailCount > maxFailuresBeforeLock) {
                    int lockDurationMills = configurationService.getIntegerValue(
                            ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID,
                            ConfigurationProperty.LOGIN_LOCK_DURATION_FAILED_AUTH,
                            300000);

                    Date lockedUntilDate = new Date(new Date().getTime() + lockDurationMills);
                    log.warn("User: " + email + " has passed maximum login attempts before account lock. Locking account until: " + lockedUntilDate.toString());

                    Map<String, Object> templateParams = new HashMap<>();
                    templateParams.put("ACCOUNT_EMAIL", email);
                    templateParams.put("ACCOUNT_LOCK_DURATION", (lockDurationMills / 1000 / 60) + " minutes");
                    templateParams.put("PASSWORD_RESET_URL", baseUrl + AccountUrl.PASSWORD_RESET);

                    emailNotificationService.send(email, NotificationType.ACCOUNT_LOCKED, templateParams);

                    discappUserRepository.updateDiscAppUserPasswordFailCountAndLastPasswordFailDateAndLockedUntilDateById(
                            user.getId(),
                            currentFailCount,
                            new Date(),
                            lockedUntilDate);
                } else {
                    //increment login failure count.
                    log.warn("User: " + email + " failed authentication but is not at max attempt: "
                            + maxFailuresBeforeLock + " current fail count: " + currentFailCount);
                    discappUserRepository.updateDiscAppUserPasswordFailCountAndLastPasswordFailDateById(
                            user.getId(),
                            currentFailCount,
                            new Date());
                }

            }
        } catch (Exception ex) {
            log.error("Failed to update password fail count for email: " + email + " :: " + ex.getMessage(), ex);
        }
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

        String adminEmail = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_ADMIN_ADDRESS, null);
        if (adminEmail == null) {
            log.error("Could not find admin email address in configuration property: "
                    + ConfigurationProperty.EMAIL_ADMIN_ADDRESS.getPropName() + " : new account email notification not sent.");
            return;
        }

        emailNotificationService.send(adminEmail, NotificationType.NEW_ACCOUNT_CREATED, templateParams);
    }
}
