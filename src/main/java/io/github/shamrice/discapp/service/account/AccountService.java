package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.model.PasswordReset;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import io.github.shamrice.discapp.data.repository.PasswordResetRepository;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
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
            log.warn("Password reset code entered for email: " + email + " does not match the one on file. Actual: "
                    + passwordReset.getCode() + " :: attempted: " + resetCode);
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

    public boolean createPasswordResetRequest(String email, String passwordResetUrl) {

        if (discAppUserDetailsService.getByEmail(email) == null) {
            log.warn("Attempted to reset password for email: " + email + " but no such disc app user exists.");
            return false;
        }

        PasswordReset passwordReset = createNewPasswordResetRequest(email);

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

    private PasswordReset createNewPasswordResetRequest(String email) {
        //delete existing if exists.
        passwordResetRepository.deleteByEmail(email);

        Calendar date = Calendar.getInstance();
        long t = date.getTimeInMillis();
        Date expDt = new Date(t + (60 * 60000)); //1 hour

        String generatedKey = UUID.randomUUID().toString();
        int generatedCode = new Random().nextInt(1000000);

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setEmail(email);
        passwordReset.setCreateDt(new Date());
        passwordReset.setExpDt(expDt);
        passwordReset.setKey(generatedKey);
        passwordReset.setCode(generatedCode);
        passwordReset.setIsRedeemed(false);

        return passwordResetRepository.save(passwordReset);
    }

}
