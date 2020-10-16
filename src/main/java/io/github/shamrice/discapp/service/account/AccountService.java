package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.model.PasswordReset;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import io.github.shamrice.discapp.data.repository.PasswordResetRepository;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.utility.email.EmailNotificationQueueService;
import io.github.shamrice.discapp.service.utility.email.TemplateEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    private static final String PASSWORD_RESET_URL = "PASSWORD_RESET_URL";
    private static final String PASSWORD_RESET_CODE = "PASSWORD_RESET_CODE";

    public List<Owner> listOwners() {
        return ownerRepository.findAll();
    }

    public Owner saveOwner(Owner owner) {
        if (owner != null) {
            log.info("Saving owner " + owner.toString());
            if (owner.getCreateDt() == null) {
                owner.setCreateDt(new Date());
            }
            owner.setModDt(new Date());
            return ownerRepository.save(owner);
        }
        log.error("Owner to save cannot be null. Returning null.");
        return null;
    }

    public Owner getOwnerById(Long ownerId) {

        if (ownerId == null) {
            log.warn("Owner id is null. Cannot get owner by null ID. Returning null.");
            return null;
        }

        try {
            Optional<Owner> owner = ownerRepository.findById(ownerId);
            if (owner.isPresent()) {
                return owner.get();
            }
            log.info("No owner record found for owner id: " + ownerId);
        } catch (Exception ex) {
            log.error("Failed to find owner by id: " + ownerId + " :: " + ex.getMessage());
        }
        return null;
    }

    public Owner getOwnerByEmail(String email) {
        return ownerRepository.findOneByEmail(email);
    }

    public boolean performPasswordReset(String resetKey, int resetCode, String email, String newPassword) {
        PasswordReset passwordReset = passwordResetRepository.findOneByEmailAndKey(email, resetKey);
        if (passwordReset == null || passwordReset.getIsRedeemed()) {
            log.warn("Failed to find non-redeemed password reset for email: " + email + " with key: " + resetKey);
            return false;
        }

        if (passwordReset.getExpDt().before(new Date())) {
            log.warn("Password reset window has closed for email: " + email + " on " + passwordReset.getExpDt().toString());
            return false;
        }

        if (!passwordReset.getCode().equals(resetCode)) {
            log.warn("Password reset code entered for email: " + email
                    + " does not match the one on file. Attempted:" + resetCode);
            return false;
        }

        DiscAppUser user = discAppUserDetailsService.getByEmail(email);
        if (user == null) {
            log.warn("Failed to find Disc App user for email address: " + email);
            return false;
        }

        user.setPassword(newPassword);
        user.setPasswordFailCount(0);
        user.setLockedUntilDate(null);
        if (!discAppUserDetailsService.saveDiscAppUser(user)) {
            log.error("Failed to update password for user: " + email);
            return false;
        }

        passwordReset.setIsRedeemed(true);
        passwordResetRepository.save(passwordReset);

        log.info("Successfully reset password for user: " + email);
        return true;
    }

    public boolean performSystemAccountPasswordReset(String resetKey, int resetCode, String email, long appId, String newPassword) {
        PasswordReset passwordReset = passwordResetRepository.findOneByEmailAndKeyAndApplicationId(email, resetKey, appId);
        if (passwordReset == null || passwordReset.getIsRedeemed()) {
            log.warn("Failed to find non-redeemed system account password reset for owner email: " + email + " with key: "
                    + resetKey + " and appId: " + appId);
            return false;
        }

        if (passwordReset.getExpDt().before(new Date())) {
            log.warn("System account Password reset window has closed for owner email: " + email + " on " + passwordReset.getExpDt().toString());
            return false;
        }

        if (!passwordReset.getCode().equals(resetCode)) {
            log.warn("Password reset code entered for owner email: " + email
                    + " does not match the one on file. :: attempted: " + resetCode);
            return false;
        }

        if (!applicationService.isOwnerOfApp(appId, email)) {
            log.warn("System account for appId: " + appId + " cannot be reset even though fields are correct because "
                    + email + " does not own the application.");
            return false;
        }

        //system account email address column is app id... I know.. it's gross
        DiscAppUser user = discAppUserDetailsService.getByEmail(String.valueOf(appId));
        if (user == null) {
            log.warn("Failed to find System user for appId: " + appId);
            return false;
        }

        user.setPassword(newPassword);
        user.setPasswordFailCount(0);
        user.setLockedUntilDate(null);
        if (!discAppUserDetailsService.saveDiscAppUser(user)) {
            log.error("Failed to update system account password for appId: " + appId);
            return false;
        }

        passwordReset.setIsRedeemed(true);
        passwordResetRepository.save(passwordReset);

        log.info("Successfully reset system account password for appId: " + appId + " owned by: " + email);
        return true;
    }

    public boolean createSystemAccountPasswordResetRequest(String ownerEmail, Long appId, String passwordResetUrl) {

        if (discAppUserDetailsService.getByEmail(appId.toString()) == null) {
            log.warn("Attempted to reset password for disc app admin for appId: " + appId + " but no such disc app exists.");
            return false;
        }

        if (!applicationService.isOwnerOfApp(appId, ownerEmail)) {
            log.warn("Attempted to reset admin system account for appId: " + appId + " with owner email: "
                    + ownerEmail + ". But that is not the owner email for that application.");
            return false;
        }

        PasswordReset passwordReset = createNewPasswordResetRequest(ownerEmail, appId);

        if (passwordReset != null) {
            Map<String, Object> emailParams = new HashMap<>();
            emailParams.put(PASSWORD_RESET_URL, passwordResetUrl + "/" + passwordReset.getKey());
            emailParams.put(PASSWORD_RESET_CODE, passwordReset.getCode());

            TemplateEmail passwordResetEmail = new TemplateEmail(ownerEmail, NotificationType.PASSWORD_RESET, emailParams, false);
            EmailNotificationQueueService.addTemplateEmailToSend(passwordResetEmail);
            return true;

        } else {
            log.error("Failed to create new admin system account password reset request for appId: "
                    + appId + " with owner email: " + ownerEmail);
        }

        return false;
    }

    public boolean createPasswordResetRequest(String email, String passwordResetUrl) {

        if (discAppUserDetailsService.getByEmail(email) == null) {
            log.warn("Attempted to reset password for email: " + email + " but no such disc app user exists.");
            return false;
        }

        PasswordReset passwordReset = createNewPasswordResetRequest(email, null);

        if (passwordReset != null) {
            Map<String, Object> emailParams = new HashMap<>();
            emailParams.put(PASSWORD_RESET_URL, passwordResetUrl + "/" + passwordReset.getKey());
            emailParams.put(PASSWORD_RESET_CODE, passwordReset.getCode());

            TemplateEmail passwordResetEmail = new TemplateEmail(email, NotificationType.PASSWORD_RESET, emailParams, false);
            EmailNotificationQueueService.addTemplateEmailToSend(passwordResetEmail);
            return true;

        } else {
            log.error("Failed to create new password reset request for email: " + email);
        }

        return false;
    }

    private PasswordReset createNewPasswordResetRequest(String email, Long appId) {
        //delete existing if exists.
        passwordResetRepository.deleteByEmail(email);

        Calendar date = Calendar.getInstance();
        long t = date.getTimeInMillis();
        Date expDt = new Date(t + (60 * 60000)); //1 hour

        String generatedKey = UUID.randomUUID().toString();
        int generatedCode = new Random().nextInt(1000000);

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setEmail(email);
        passwordReset.setApplicationId(appId);
        passwordReset.setCreateDt(new Date());
        passwordReset.setExpDt(expDt);
        passwordReset.setKey(generatedKey);
        passwordReset.setCode(generatedCode);
        passwordReset.setIsRedeemed(false);

        return passwordResetRepository.save(passwordReset);
    }

}
