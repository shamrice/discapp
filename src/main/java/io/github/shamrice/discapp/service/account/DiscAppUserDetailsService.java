package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.UserRegistration;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.data.repository.UserRegistrationRepository;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.utility.email.EmailNotificationQueueService;
import io.github.shamrice.discapp.service.utility.email.TemplateEmail;
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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_USER_REGISTRATION;
import static java.util.UUID.randomUUID;

@Service
@Slf4j
public class DiscAppUserDetailsService implements UserDetailsService {

    @Autowired
    private DiscAppUserRepository discappUserRepository;

    @Autowired
    private UserRegistrationRepository userRegistrationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;


    @Value("${discapp.root.email}")
    private String rootAccountEmail;

    @Value("${discapp.security.bcrypt.strength}")
    private int bcryptStrength;

    private final static String SITE_URL = "SITE_URL";
    private final static String USER_REGISTRATION_URL = "USER_REGISTRATION_URL";

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

        boolean isRoot = false;
        if (rootAccountEmail != null && !rootAccountEmail.trim().isEmpty()) {
            isRoot = rootAccountEmail.equalsIgnoreCase(user.getEmail());
        }

        //principal is returned back and verified by "spring magic"
        return new DiscAppUserPrincipal(user, isRoot, applicationService);
    }

    public List<DiscAppUser> searchByUsername(String searchTerm, boolean searchUserAccounts) {
        List<DiscAppUser> results = discappUserRepository.findByUsernameContainingIgnoreCaseAndIsUserAccount(searchTerm, searchUserAccounts);
        results.removeIf(user -> this.rootAccountEmail.equalsIgnoreCase(user.getEmail()) || !user.getEnabled());
        return results;
    }

    public List<DiscAppUser> searchByEmail(String searchTerm, boolean searchUserAccounts) {
        List<DiscAppUser> results = discappUserRepository.findByEmailContainingIgnoreCaseAndIsUserAccount(searchTerm, searchUserAccounts);
        results.removeIf(user -> this.rootAccountEmail.equalsIgnoreCase(user.getEmail()) || !user.getEnabled());
        return results;
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

        if (user != null) {

            boolean isNewUser = false;
            if (user.getId() == null || user.getId() <= 0) {
                isNewUser = true;
            }

            try {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    String plainPassword = user.getPassword();
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(bcryptStrength);
                    String encodedPassword = passwordEncoder.encode(plainPassword);
                    user.setPassword(encodedPassword);
                }

                user.setUsername(user.getUsername().trim());
                user.setEmail(user.getEmail().trim());
                user.setModDt(new Date());
                user.setPasswordFailCount(0);

                DiscAppUser createdUser = discappUserRepository.save(user);
                if (createdUser != null && createdUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                    log.info("Saved disc app user: " + createdUser.getEmail() + " : username: "
                            + createdUser.getUsername());

                    //create default user configurations
                    if (isNewUser) {
                        log.info("Setting default user configuration values for new user: " + createdUser.getId());
                        configurationService.setDefaultUserConfigurationValuesForUser(createdUser.getId());
                    }

                    return true;
                }
            } catch (Exception ex) {
                log.error("Failed to save disc app user: " + user.getEmail() + " :: " + ex.getMessage(), ex);
            }
        }

        return false;
    }

    public void setLastLoginDateToNow(long userId) {
        try {
            log.info("Setting last login date to now for userId: " + userId);
            discappUserRepository.updateDiscAppUserLastLoginDateAndPasswordFailCountById(userId, new Date(), 0);
        } catch (Exception ex) {
            log.error("Failed to set last log in date for userId: " + userId + " :: " + ex.getMessage(), ex);
        }
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

                    //todo : send this to email queue
                    Date lockedUntilDate = new Date(new Date().getTime() + lockDurationMills);
                    log.warn("User: " + email + " has passed maximum login attempts before account lock. Locking account until: " + lockedUntilDate.toString());

                    Map<String, Object> templateParams = new HashMap<>();
                    templateParams.put("ACCOUNT_EMAIL", email);
                    templateParams.put("ACCOUNT_LOCK_DURATION", (lockDurationMills / 1000 / 60) + " minutes");
                    templateParams.put("PASSWORD_RESET_URL", baseUrl + AccountUrl.ACCOUNT_PASSWORD);

                    TemplateEmail accountLockedEmail = new TemplateEmail(email, NotificationType.ACCOUNT_LOCKED, templateParams, false);
                    EmailNotificationQueueService.addTemplateEmailToSend(accountLockedEmail);

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

    public boolean deactivateUser(long userId) {
        DiscAppUser user = discappUserRepository.findById(userId).orElse(null);
        if (user != null) {

            String deactivatedUsername = user.getUsername() + "_DELETED_" + UUID.randomUUID().toString();
            String deactivatedEmail = user.getEmail() + "_DELETED_" + UUID.randomUUID().toString();

            user.setEnabled(false);
            user.setUsername(deactivatedUsername);
            user.setEmail(deactivatedEmail);
            user.setShowEmail(false);
            user.setModDt(new Date());

            discappUserRepository.save(user);
            log.info("User deactivated: " + user.toString());
            return true;
        } else {
            log.warn("User id: " + userId + " does not exist and cannot be deactivated.");
            return false;
        }
    }

    public boolean updateDiscAppUserEnabled(long userId, boolean isEnabled) {
        log.info("Updating user enabled status: userId: " + userId + " : isEnabled: " + isEnabled);
        return discappUserRepository.updateDiscAppUserEnabled(userId, isEnabled, new Date()) > 0;
    }

    public void createNewUserRegistrationRequest(String newUserEmail, String baseSiteUrl) {

        String registrationKey = randomUUID().toString();

        //check if they have an existing record, if so... refresh it otherwise create new.
        UserRegistration newUserRegistration = userRegistrationRepository.findOneByEmail(newUserEmail);

        if (newUserRegistration == null) {
            newUserRegistration = new UserRegistration();
            newUserRegistration.setEmail(newUserEmail);
            newUserRegistration.setCreateDt(new Date());
        }
        newUserRegistration.setRedeemed(false);
        newUserRegistration.setRedeemDt(null);
        newUserRegistration.setKey(registrationKey);

        userRegistrationRepository.save(newUserRegistration);
        log.info("Created new user registration record: " + newUserRegistration.toString());

        String encodedEmail = UriUtils.encode(newUserEmail, StandardCharsets.UTF_8);

        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put(SITE_URL, baseSiteUrl);
        templateParams.put(USER_REGISTRATION_URL, baseSiteUrl + ACCOUNT_USER_REGISTRATION + "?email=" + encodedEmail + "&key=" + registrationKey);

        TemplateEmail newUserCreatedEmail = new TemplateEmail(newUserEmail, NotificationType.NEW_ACCOUNT_CREATED, templateParams, false);
        EmailNotificationQueueService.addTemplateEmailToSend(newUserCreatedEmail);
    }

    public boolean redeemNewUserRegistrationKey(String email, String registrationKey) {
        UserRegistration userRegistration = userRegistrationRepository.findOneByEmailAndKey(email, registrationKey);
        if (userRegistration != null) {
            if (userRegistration.isRedeemed()) {
                log.warn("User registration is already redeemed. Not redeeming again. Returning true");
                return true;
            }
            DiscAppUser user = discappUserRepository.findByEmail(email);
            if (user != null) {
                if (updateDiscAppUserEnabled(user.getId(), true)) {
                    userRegistration.setRedeemDt(new Date());
                    userRegistration.setRedeemed(true);
                    userRegistrationRepository.save(userRegistration);
                    log.info("Successfully redeemed activation and enabled user: " + email);
                    return true;
                }
            }
        }
        log.warn("Failed to activate user registration for email: " + email + " : key: " + registrationKey);
        return false;
    }
}
