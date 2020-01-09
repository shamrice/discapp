package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.STATUS;
import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_PASSWORD;
import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_PASSWORD_RESET;

@Controller
@Slf4j
public class AccountPasswordResetController extends AccountController {

    @PostMapping(ACCOUNT_PASSWORD_RESET)
    public ModelAndView postPasswordResetForm(@PathVariable(name = "resetKey") String resetKey,
                                              @RequestParam(name = "email") String email,
                                              @RequestParam(name = "password") String newPassword,
                                              @RequestParam(name = "confirmPassword") String confirmPassword,
                                              @RequestParam(name = "resetCode") String resetCode,
                                              @RequestParam(name = "reCaptchaResponse") String reCaptchaResponse,
                                              ModelMap modelMap) {
        log.debug("Password reset post: resetKey = " + resetKey + " email=" + email + " resetCode=" + resetCode);

        modelMap.addAttribute(PASSWORD_RESET_KEY, resetKey);
        modelMap.addAttribute(EMAIL, email);
        modelMap.addAttribute(PASSWORD_RESET_CODE, resetCode);

        if (!inputHelper.verifyReCaptchaResponse(reCaptchaResponse)) {
            log.warn("Failed to create password reset request for " + email
                    + " due to ReCaptcha verification failure.");
            modelMap.addAttribute(ERROR_MESSAGE, "Password reset failed. Please resubmit request.");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        if (email == null || email.trim().isEmpty()) {
            modelMap.addAttribute(ERROR_MESSAGE, "Invalid email address.");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 8) {
            modelMap.addAttribute(ERROR_MESSAGE, "Password must be at least 8 characters");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        if (!newPassword.equals(confirmPassword)) {
            modelMap.addAttribute(ERROR_MESSAGE, "New password confirmation does not match.");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        if (resetCode == null || resetCode.trim().isEmpty()) {
            modelMap.addAttribute(ERROR_MESSAGE, "Valid password reset code is required.");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        int resetCodeInt = 0;
        try {
            resetCodeInt = Integer.parseInt(resetCode.trim());
        } catch (NumberFormatException numFormatEx) {
            log.error("Failed to parse password reset code: " + resetCode + " for email: " + email + " :: "
                    + numFormatEx.getMessage(), numFormatEx);
            modelMap.addAttribute(ERROR_MESSAGE, "Valid password reset code is required.");
            return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
        }

        if (accountService.performPasswordReset(resetKey, resetCodeInt, email, newPassword)) {
            modelMap.addAttribute(STATUS, "Password successfully reset.");
            return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
        }

        modelMap.addAttribute(STATUS, "Password reset failed. Please resubmit request.");
        return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
    }

    @GetMapping(ACCOUNT_PASSWORD_RESET)
    public ModelAndView getPasswordResetFormView(@PathVariable(name = "resetKey") String resetKey,
                                                 ModelMap modelMap) {
        modelMap.addAttribute(PASSWORD_RESET_KEY, resetKey);
        return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
    }

    @GetMapping(ACCOUNT_PASSWORD)
    public ModelAndView getAccountPasswordResetRequestView(ModelMap modelMap) {
        return new ModelAndView("account/password/resetPasswordRequest");
    }

    @PostMapping(ACCOUNT_PASSWORD)
    public ModelAndView postAccountPasswordRequestView(@RequestParam(name = "email") String email,
                                                       @RequestParam(name = "reCaptchaResponse") String reCaptchaResponse,
                                                       ModelMap modelMap,
                                                       HttpServletRequest request) {

        if (email != null && !email.trim().isEmpty()) {
            log.info("Attempting to create new password reset request for email: " + email);

            //always tell client that password request was success to keep brute force email searching impossible.
            modelMap.addAttribute(STATUS, "Please check the email address you provided for the next steps in your password reset request.");

            if (!inputHelper.verifyReCaptchaResponse(reCaptchaResponse)) {
                log.warn("Failed to create password reset request for " + email
                        + " due to ReCaptcha verification failure.");
                return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
            }

            //don't allow password reset requests to be generated for system admin accounts.
            DiscAppUser user = discAppUserDetailsService.getByEmail(email.trim());
            if (user != null && !user.getIsUserAccount()) {
                log.warn("Failed to create password reset request for email: " + email + " :: user is system admin account.");
                return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
            }

            if (!accountService.createPasswordResetRequest(email, webHelper.getBaseUrl(request) + ACCOUNT_PASSWORD)) {
                log.warn("Failed to create password request for email: " + email);
            }
            return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
        }
        modelMap.addAttribute(STATUS, "An Email address is required to reset your password.");
        return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
    }

}
