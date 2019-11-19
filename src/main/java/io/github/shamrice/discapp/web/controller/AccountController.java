package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.AccountViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.url.AppUrl.APP_SEARCH_URL;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.MAINTENANCE_PAGE;

@Controller
@Slf4j
public class AccountController {

    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";
    private static final String DELETE = "delete";

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private WebHelper webHelper;

    @GetMapping("/account/delete/status")
    public ModelAndView getAccountDeleteStatus(ModelMap modelMap) {
        return new ModelAndView("account/delete/deleteAccountStatus", modelMap);
    }

    @GetMapping("/account/delete")
    public ModelAndView getAccountDelete(ModelMap modelMap) {
        return new ModelAndView("account/delete/deleteAccount", modelMap);
    }

    @PostMapping("/account/delete")
    public ModelAndView postAccountDelete(@RequestParam(name = "reCaptchaResponse") String reCaptchaResponse,
                                          ModelMap modelMap, HttpSession session) {

        if (!inputHelper.verifyReCaptchaResponse(reCaptchaResponse)) {
            log.warn("Delete account reCaptcha verification failed.");
            modelMap.addAttribute("status", "User deletion failed.");
            return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
        }

        String username = accountHelper.getLoggedInEmail();
        if (username != null) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(username);
            if (user != null) {

                //mark owner as disabled. (when owner is disabled, all associated apps will be as well)
                if (user.getOwnerId() != null) {
                    Owner appOwner = accountService.getOwnerById(user.getOwnerId());
                    if (appOwner != null) {
                        appOwner.setEnabled(false);
                        log.info("Delete Account : Disabling account owner id: " + appOwner.toString());
                        if (accountService.saveOwner(appOwner) != null) {
                            log.info("Successfully marked owner " + appOwner.toString() + " as disabled.");
                        } else {
                            log.error("Failed to mark owner: " + appOwner.toString() + " as disabled.");
                        }
                    }
                }

                //mark disc app account as disabled
                if (discAppUserDetailsService.updateDiscAppUserEnabled(user.getId(), false)) {
                    log.info("Successfully disabled disc app user: " + user.toString());
                    modelMap.addAttribute("status", "User successfully deleted.");

                    //invalidate current session.
                    session.invalidate();

                    return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
                } else {
                    log.error("Failed to disable disc app user: " + user.toString());
                }
            }
        }
        modelMap.addAttribute("status", "User deletion failed.");
        return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
    }

    @PostMapping("/password/reset/{resetKey}")
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

    @GetMapping("/password/reset/{resetKey}")
    public ModelAndView getPasswordResetFormView(@PathVariable(name = "resetKey") String resetKey,
                                             ModelMap modelMap) {
        modelMap.addAttribute(PASSWORD_RESET_KEY, resetKey);
        return new ModelAndView("account/password/passwordResetForm", "model", modelMap);
    }

    @GetMapping("/account/password")
    public ModelAndView getAccountPasswordResetRequestView(ModelMap modelMap) {
        return new ModelAndView("account/password/resetPasswordRequest");
    }

    @PostMapping("/account/password")
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

            if (!accountService.createPasswordResetRequest(email, webHelper.getBaseUrl(request) + "/password/reset")) {
                log.warn("Failed to create password request for email: " + email);
            }
            return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
        }
        modelMap.addAttribute(STATUS, "An Email address is required to reset your password.");
        return new ModelAndView("account/password/passwordResetStatus", "model", modelMap);
    }

    @GetMapping("/account/create")
    public ModelAndView getCreateAccount(@ModelAttribute AccountViewModel accountViewModel,
                                         ModelMap modelMap) {
        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping("/account/create")
    public ModelAndView postCreateAccount(@ModelAttribute AccountViewModel accountViewModel, ModelMap modelMap) {

        if (accountViewModel != null) {
            log.info("Attempting to create new account with username: " + accountViewModel.getUsername());

            if (!inputHelper.verifyReCaptchaResponse(accountViewModel.getReCaptchaResponse())) {
                log.warn("Failed to create new account for " + accountViewModel.getUsername()
                        + " due to ReCaptcha verification failure.");
                accountViewModel.setErrorMessage("Failed to create account.");
                return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
            }

            String username = inputHelper.sanitizeInput(accountViewModel.getUsername());
            String password = inputHelper.sanitizeInput(accountViewModel.getPassword());

            if (!accountViewModel.getPassword().equals(accountViewModel.getConfirmPassword())
                    || !accountViewModel.getPassword().equals(password)) {

                accountViewModel.setErrorMessage("Passwords do not match or contain invalid characters.");
                return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
            }

            if (password.length() < 8) {
                accountViewModel.setErrorMessage("Passwords must be at least eight characters in length.");
                return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
            }

            //let user know if username has already been taken before attempting to create new user.
            if (discAppUserDetailsService.getByUsername(username) != null) {
                log.warn("Account creation failed for user: " + accountViewModel.getEmail()
                        + " because username is already taken. Username: " + username);
                accountViewModel.setErrorMessage("Display name: " + username + " has already been taken. Please specify a different disc app display name.");
                return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
            }

            //let user know if email address has already been taken before attempting to create
            if (discAppUserDetailsService.getByEmail(accountViewModel.getEmail()) != null) {
                log.warn("Account creation failed for user: " + accountViewModel.getEmail()
                        + " because email address is already taken.");
                accountViewModel.setErrorMessage("Email: " + accountViewModel.getEmail() + " has already been taken. Please specify a different email address.");
                return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
            }

            DiscAppUser newUser = new DiscAppUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setIsAdmin(accountViewModel.isAdmin());
            newUser.setEmail(accountViewModel.getEmail());
            newUser.setShowEmail(accountViewModel.isShowEmail());
            newUser.setEnabled(accountViewModel.isEnabled());
            newUser.setOwnerId(accountViewModel.getOwnerId());
            newUser.setIsUserAccount(true);
            newUser.setModDt(new Date());
            newUser.setCreateDt(new Date());

            if (discAppUserDetailsService.saveDiscAppUser(newUser)) {
                log.info("Successfully created new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());
                return new ModelAndView("account/createAccountSuccess");
            } else {
                log.error("Failed to create new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());
                accountViewModel.setErrorMessage("Failed to create new account for user: " + accountViewModel.getEmail());
            }
        }

        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);

    }

    @GetMapping("/account/createAccountSuccess")
    public String getCreateSuccess(ModelMap modelMap) {
        modelMap.addAttribute("status", "Successfully created new user");
        return "account/createAccountSuccess";
    }


    @GetMapping("/account/application")
    public ModelAndView getAccountApplication(@ModelAttribute AccountViewModel accountViewModel,
                                         ModelMap modelMap) {

        String email = accountHelper.getLoggedInEmail();

        if (accountViewModel != null && email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);

            accountViewModel.setMaxDiscApps(configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAX_APPS_PER_ACCOUNT, 1));
            accountViewModel.setUsername(user.getUsername());
            accountViewModel.setAdmin(user.getIsAdmin());
            accountViewModel.setCreateDt(user.getCreateDt());
            accountViewModel.setModDt(user.getModDt());
            accountViewModel.setEmail(user.getEmail());
            accountViewModel.setEnabled(user.getEnabled());
            accountViewModel.setShowEmail(user.getShowEmail());

            if (user.getOwnerId() != null && user.getOwnerId() > 0) {
                Owner owner = accountService.getOwnerById(user.getOwnerId());

                if (owner != null) {

                    accountViewModel.setOwnerId(owner.getId());
                    accountViewModel.setOwnerFirstName(owner.getFirstName());
                    accountViewModel.setOwnerLastName(owner.getLastName());
                    accountViewModel.setOwnerEmail(owner.getEmail());
                    accountViewModel.setOwnerPhone(owner.getPhone());

                    //get list of applications for an account.
                    List<Application> apps = applicationService.getByOwnerId(owner.getId());
                    List<AccountViewModel.AccountApplication> accountApplications = new ArrayList<>();
                    for (Application app : apps) {

                        String appStatus = DISABLED;
                        if (app.getEnabled())
                            appStatus = ENABLED;

                        String appSearchStatus = DISABLED;
                        if (app.getSearchable()) {
                            appSearchStatus = ENABLED;
                        }

                        AccountViewModel.AccountApplication application = new AccountViewModel.AccountApplication(
                                app.getName(), app.getId(), appStatus, appSearchStatus
                        );
                        accountApplications.add(application);
                    }

                    //sort them by app id.
                    accountApplications.sort((a1, a2) -> {
                        if (a1.getApplicationId().equals(a2.getApplicationId())) {
                            return 0;
                        }
                        return a1.getApplicationId() > a2.getApplicationId() ? -1 : 1;
                    });

                    accountViewModel.setAccountApplications(accountApplications);
                }
            }
        }

        return new ModelAndView("account/application/manageApplications", "accountViewModel", accountViewModel);
    }


    @GetMapping("/account/modify")
    public ModelAndView getAccountModify(@ModelAttribute AccountViewModel accountViewModel,
                                         HttpServletRequest request,
                                         ModelMap modelMap) {

        String email = accountHelper.getLoggedInEmail();

        if (accountViewModel != null && email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);

            accountViewModel.setMaxDiscApps(configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAX_APPS_PER_ACCOUNT, 1));
            accountViewModel.setUsername(user.getUsername());
            accountViewModel.setAdmin(user.getIsAdmin());
            accountViewModel.setCreateDt(user.getCreateDt());
            accountViewModel.setModDt(user.getModDt());
            accountViewModel.setEmail(user.getEmail());
            accountViewModel.setEnabled(user.getEnabled());
            accountViewModel.setShowEmail(user.getShowEmail());

            if (user.getOwnerId() != null && user.getOwnerId() > 0) {
                Owner owner = accountService.getOwnerById(user.getOwnerId());

                if (owner != null) {

                    accountViewModel.setOwnerId(owner.getId());
                    accountViewModel.setOwnerFirstName(owner.getFirstName());
                    accountViewModel.setOwnerLastName(owner.getLastName());
                    accountViewModel.setOwnerEmail(owner.getEmail());
                    accountViewModel.setOwnerPhone(owner.getPhone());
                }
            }

            List<UserPermission> userPermissions = applicationService.getAllApplicationPermissionsForUser(user.getId());
            if (userPermissions != null) {
                List<AccountViewModel.AccountApplication> moderatingApplications = new ArrayList<>();

                for (UserPermission userPermission : userPermissions) {

                    if (userPermission.getUserPermissions().contains(io.github.shamrice.discapp.service.application.permission.UserPermission.EDIT)) {

                        Application application = applicationService.get(userPermission.getApplicationId());
                        if (application != null) {
                            AccountViewModel.AccountApplication accountApplication = new AccountViewModel.AccountApplication(
                                    application.getName(),
                                    application.getId(),
                                    ENABLED,
                                    ENABLED
                            );
                            moderatingApplications.add(accountApplication);
                        }
                    }
                }
                String baseUrl = webHelper.getBaseUrl(request);
                accountViewModel.setBaseEditorUrl(baseUrl + "/admin/disc-edit.cgi?id=");

                accountViewModel.setModeratingApplications(moderatingApplications);
            }
        }

        return new ModelAndView("account/modifyAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping("/account/modify/password")
    public ModelAndView postAccountModifyPassword(@ModelAttribute AccountViewModel accountViewModel,
                                                  HttpServletRequest request,
                                                  ModelMap modelMap) {
        if (accountViewModel != null) {
            String email = accountHelper.getLoggedInEmail();

            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {

                String currentPassword = accountViewModel.getPassword();
                String newPassword = accountViewModel.getNewPassword();
                String confirmNewPassword = accountViewModel.getConfirmPassword();

                if ((currentPassword == null || currentPassword.trim().isEmpty())
                        || (newPassword == null || newPassword.trim().isEmpty())
                        || (confirmNewPassword == null || confirmNewPassword.isEmpty())) {

                    accountViewModel.setErrorMessage("Password must be at least 8 characters.");
                    return getAccountModify(accountViewModel, request, modelMap);
                }

                if (newPassword.length() < 8) { //todo : set length in some constant somewhere or config...
                    accountViewModel.setErrorMessage("Password must be at least 8 characters.");
                    return getAccountModify(accountViewModel, request, modelMap);
                }

                if (!newPassword.trim().equals(confirmNewPassword.trim())) {
                    accountViewModel.setErrorMessage("Passwords do not match.");
                    return getAccountModify(accountViewModel, request, modelMap);
                } else {

                    //verify passwords entered are correct.
                    if (!BCrypt.checkpw(accountViewModel.getPassword(), user.getPassword())) {
                        log.error("Cannot update account password. Original password does not match existing password for account.");
                        accountViewModel.setErrorMessage("Failed to update password");
                        return getAccountModify(accountViewModel, request, modelMap);
                    }

                    user.setPassword(newPassword.trim());
                    if (discAppUserDetailsService.saveDiscAppUser(user)) {
                        accountViewModel.setInfoMessage("Password successfully updated.");
                        return getAccountModify(accountViewModel, request, modelMap);
                    } else {
                        log.error("Failed to update password for user: " + user.getEmail() + " : userId: " + user.getId());
                        accountViewModel.setErrorMessage("Failed to update password.");
                        return getAccountModify(accountViewModel, request, modelMap);
                    }
                }
            }
        }

        return new ModelAndView("redirect:/account/modify");
    }

    @GetMapping("/account/modify/account")
    public ModelAndView getAccountModifyAccount(@ModelAttribute AccountViewModel accountViewModel,
                                         HttpServletRequest request,
                                         ModelMap modelMap) {
        return getAccountModify(accountViewModel, request, modelMap);
    }

    @PostMapping("/account/modify/account")
    public ModelAndView postAccountModify(@ModelAttribute AccountViewModel accountViewModel,
                                          HttpServletRequest request,
                                          ModelMap modelMap) {
        if (accountViewModel != null) {

            String username = inputHelper.sanitizeInput(accountViewModel.getUsername());
            String email = accountHelper.getLoggedInEmail();

            if (username == null || username.trim().isEmpty()) {
                log.warn("Account: " + email + " attempted to create an empty disc app username.");
                accountViewModel.setErrorMessage("Disc App display name cannot be empty.");
                return getAccountModify(accountViewModel, request, modelMap);
            }

            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {

                //let user know if username has already been taken before attempting to modify user.
                //and ignore if it exists what is the current username.
                if (!user.getUsername().equalsIgnoreCase(username)
                        && (discAppUserDetailsService.getByUsername(username) != null)) {
                    log.warn("Account modification failed for user: " + email
                            + " because username is already taken. Username: " + username);
                    accountViewModel.setErrorMessage("Display name: " + username + " has already been taken. Please specify a different disc app display name.");
                    return getAccountModify(accountViewModel, request, modelMap);
                }

                boolean showEmail = accountViewModel.isShowEmail();

                if (username.trim().isEmpty()) {
                    username = user.getUsername();
                }

                if (!discAppUserDetailsService.updateDiscAppUser(user.getId(), username, showEmail)) {
                    log.error("Failed to update user : " + email + ". Changes will not be saved.");
                    accountViewModel.setErrorMessage("Failed to update user.");
                } else {
                    accountViewModel.setInfoMessage("Successfully updated user information.");
                    log.info("User " + email + " account information was updated.");

                }
            }
        }

        return new ModelAndView("redirect:/account/modify");
    }


    @PostMapping("/account/modify/application")
    public ModelAndView postApplicationModify(@ModelAttribute AccountViewModel accountViewModel,
                                              ModelMap modelMap) {
        if (accountViewModel != null) {

            if (accountViewModel.getApplicationName() != null && !accountViewModel.getApplicationName().isEmpty()) {

                String email = accountHelper.getLoggedInEmail();

                if (email != null && !email.trim().isEmpty()) {
                    DiscAppUser user = discAppUserDetailsService.getByEmail(email);

                    if (user != null) {
                        List<Application> ownedApps = applicationService.getByOwnerId(user.getOwnerId());

                        for (Application app : ownedApps) {
                            if (app.getId().equals(accountViewModel.getApplicationId())) {

                                String appName = inputHelper.sanitizeInput(accountViewModel.getApplicationName());

                                app.setName(appName);
                                app.setModDt(new Date());

                                if (ENABLED.equalsIgnoreCase(accountViewModel.getApplicationStatus())) {
                                    app.setEnabled(true);
                                } else if (DELETE.equalsIgnoreCase(accountViewModel.getApplicationStatus())){
                                    app.setEnabled(false);
                                    app.setSearchable(false);
                                    app.setDeleted(true);
                                    //mark all threads as deleted in DB.
                                    threadService.deleteAllThreadsInApplication(app.getId());
                                } else {
                                    app.setEnabled(false);
                                }

                                if (ENABLED.equalsIgnoreCase(accountViewModel.getApplicationSearchStatus())) {
                                    app.setSearchable(true);
                                } else {
                                    app.setSearchable(false);
                                }

                                if (applicationService.save(app) != null) {
                                    log.info("Application id: " + app.getId() + " has been updated.");
                                    accountViewModel.setInfoMessage("Application has been updated.");
                                } else {
                                    log.error("Unable to save application changes for appId:" + app.getId());
                                    accountViewModel.setErrorMessage("Failed to update application.");
                                }
                            }
                        }
                    }
                }
            } else {
                log.error("Application name entered to be changed is either null or empty");
                accountViewModel.setErrorMessage("Cannot update application name. Name cannot be null or empty.");
            }
        } else {
            log.error("Account view model is null. Nothing to update.");
        }


        return getAccountApplication(accountViewModel, modelMap);
    }


    @PostMapping("/account/modify/owner")
    public ModelAndView postOwnerModify(@ModelAttribute AccountViewModel accountViewModel,
                                        HttpServletRequest request,
                                        ModelMap modelMap) {

        if (accountViewModel != null) {

            String email = accountHelper.getLoggedInEmail();

            if (email != null && !email.trim().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                if (user != null) {

                    Owner owner = accountService.getOwnerById(user.getOwnerId());

                    if (owner != null) {

                        String firstName = inputHelper.sanitizeInput(accountViewModel.getOwnerFirstName());
                        String lastName = inputHelper.sanitizeInput(accountViewModel.getOwnerLastName());
                        String phone = inputHelper.sanitizeInput(accountViewModel.getOwnerPhone());

                        owner.setFirstName(firstName);
                        owner.setLastName(lastName);
                        owner.setPhone(phone);
                        owner.setModDt(new Date());

                        if (accountService.saveOwner(owner) != null) {
                            log.info("Owner id " + owner.getId() + " has been updated.");
                            accountViewModel.setInfoMessage("Owner information has been updated.");
                        } else {
                            log.error("Error saving owner information for ownerId: " + owner.getId());
                            accountViewModel.setErrorMessage("Unable to save updated owner information.");
                        }
                    } else {
                        log.error("Unable to find owner for id: " + accountViewModel.getOwnerId() + " to update.");
                        accountViewModel.setErrorMessage("Unable to find owner to update.");
                    }
                }
            }
        }

        return getAccountModify(accountViewModel, request, modelMap);
    }

    @GetMapping("/account/add/application")
    public ModelAndView getAddApplication(@ModelAttribute AccountViewModel accountViewModel,
                                          ModelMap modelMap) {

        String email = accountHelper.getLoggedInEmail();

        if (accountViewModel != null && email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);

            if (user.getOwnerId() != null && user.getOwnerId() > 0) {
                Owner owner = accountService.getOwnerById(user.getOwnerId());
                if (owner != null) {
                    accountViewModel.setOwnerId(owner.getId());
                    accountViewModel.setOwnerFirstName(owner.getFirstName());
                    accountViewModel.setOwnerLastName(owner.getLastName());
                    accountViewModel.setOwnerEmail(owner.getEmail());
                    accountViewModel.setOwnerPhone(owner.getPhone());

                    List<Application> apps = applicationService.getByOwnerId(owner.getId());
                    int appLimit = configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAX_APPS_PER_ACCOUNT, 1);

                    accountViewModel.setMaxDiscApps(appLimit);

                    if (apps.size() >= appLimit) {
                        accountViewModel.setErrorMessage("Unable to add an additional application. You are already at your account limit.");
                        return getAccountApplication(accountViewModel, modelMap);
                    }
                }
            }
        }
        return new ModelAndView("account/application/createApplication", "accountViewModel", accountViewModel);
    }

    @PostMapping("/account/add/application")
    public ModelAndView postAddApplication(@ModelAttribute AccountViewModel accountViewModel,
                                           HttpServletRequest request,
                                           ModelMap modelMap) {

        if (accountViewModel != null) {

            //cancel button, return to manage applications
            if (accountViewModel.getCancel() != null && !accountViewModel.getCancel().isEmpty()) {
                return getAccountApplication(accountViewModel, modelMap);
            }

            String password = inputHelper.sanitizeInput(accountViewModel.getPassword());
            String appAdminPassword = inputHelper.sanitizeInput(accountViewModel.getApplicationAdminPassword());

            if (accountViewModel.getApplicationAdminPassword().equals(accountViewModel.getConfirmApplicationAdminPassword())
                    && accountViewModel.getPassword() != null && !accountViewModel.getPassword().isEmpty()
                    && accountViewModel.getConfirmApplicationAdminPassword() != null && !accountViewModel.getConfirmApplicationAdminPassword().isEmpty()
                    && accountViewModel.getApplicationAdminPassword() != null && !accountViewModel.getApplicationAdminPassword().isEmpty()
                    && password.equals(accountViewModel.getPassword())
                    && appAdminPassword.equals(accountViewModel.getApplicationAdminPassword())) {

                String email = accountHelper.getLoggedInEmail();

                if (email != null && !email.trim().isEmpty()) {

                    DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                    if (user != null) {

                        //verify passwords entered are correct.
                        if (!BCrypt.checkpw(accountViewModel.getPassword(), user.getPassword())) {
                            log.error("Cannot add Disc App to account. Passwords do not match existing password for account.");
                            accountViewModel.setErrorMessage("Cannot create new application. Passwords do not match.");
                            return getAccountModify(accountViewModel, request, modelMap);
                        }

                        String ownerFirstName = inputHelper.sanitizeInput(accountViewModel.getOwnerFirstName());
                        String ownerLastName = inputHelper.sanitizeInput(accountViewModel.getOwnerLastName());
                        String ownerPhone = inputHelper.sanitizeInput(accountViewModel.getOwnerPhone());
                        String appName = inputHelper.sanitizeInput(accountViewModel.getApplicationName());


                        if ((ownerFirstName == null || ownerFirstName.trim().isEmpty())
                                || (ownerLastName == null || ownerLastName.trim().isEmpty()) ) {
                            log.warn("UserId : " + user.getId() + " : email: " + user.getEmail()
                                    + " attempted to create a new owner without a first or last name.");
                            accountViewModel.setErrorMessage("Owner first name and last name are required to create an application.");
                            return getAccountModify(accountViewModel, request, modelMap);
                        }

                        if (appName == null || appName.trim().isEmpty()) {
                            log.warn("UserId : " + user.getId() + " : email: " + user.getEmail()
                                    + " attempted to create a new application without a name");
                            accountViewModel.setErrorMessage("Application name is required to create an application.");
                            return getAccountModify(accountViewModel, request, modelMap);
                        }

                        //check if owner already exists...
                        Owner owner = accountService.getOwnerById(user.getOwnerId());

                        if (owner == null) {
                            owner = new Owner();
                            owner.setFirstName(ownerFirstName);
                            owner.setLastName(ownerLastName);
                            owner.setEmail(user.getEmail()); //use same user email
                            owner.setPhone(ownerPhone);
                            owner.setEnabled(true);
                            owner.setCreateDt(new Date());
                            owner.setModDt(new Date());
                        }

                        Owner savedOwner = accountService.saveOwner(owner);
                        if (savedOwner != null) {

                            //if owner is at app limit. Return with error.
                            List<Application> apps = applicationService.getByOwnerId(owner.getId());
                            int appLimit = configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAX_APPS_PER_ACCOUNT, 1);
                            if (apps.size() >= appLimit) {
                                log.warn("Cannot add additional app, User is already at the application limit of: " + appLimit);
                                accountViewModel.setErrorMessage("You are at the max application limit of " + appLimit
                                        + ". Please delete one or more apps to create a new one.");
                                return getAccountApplication(accountViewModel, modelMap);
                            }


                            Application newApp = new Application();
                            newApp.setName(appName);
                            newApp.setOwnerId(savedOwner.getId());
                            newApp.setEnabled(true);
                            newApp.setDeleted(false);
                            newApp.setSearchable(true);
                            newApp.setCreateDt(new Date());
                            newApp.setModDt(new Date());

                            Application savedApp = applicationService.save(newApp);

                            if (savedApp != null) {

                                if (!discAppUserDetailsService.updateOwnerInformation(user.getId(),
                                        savedOwner.getId(), true)) {
                                    log.error("Failed to update disc app user id: " + user.getId()
                                            + " with new ownerId: " + savedOwner.getId());
                                }

                                //save default new epilogue for app with maintenance link
                                if (!applicationService.createDefaultEpilogue(savedApp.getId(), MAINTENANCE_PAGE, APP_SEARCH_URL)) {
                                    log.warn("Failed to create default epilogue for new appId: " + savedApp.getId());
                                }

                                //create new disc app admin user.
                                DiscAppUser newAdminAccount = new DiscAppUser();
                                newAdminAccount.setEnabled(true);
                                newAdminAccount.setIsAdmin(true);
                                newAdminAccount.setOwnerId(savedOwner.getId());
                                newAdminAccount.setShowEmail(false);
                                newAdminAccount.setEmail(savedApp.getId().toString()); //email and username set to appId
                                newAdminAccount.setUsername(UUID.randomUUID().toString());
                                newAdminAccount.setCreateDt(new Date());
                                newAdminAccount.setModDt(new Date());
                                newAdminAccount.setPassword(accountViewModel.getApplicationAdminPassword());
                                newAdminAccount.setIsUserAccount(false);

                                //save new user and don't create email notification as account is active from the get go.
                                if (!discAppUserDetailsService.saveDiscAppUser(newAdminAccount, false)) {
                                    log.error("Failed to create new admin user for new disc app: " + savedApp.getId());
                                }

                                //save default permission values for new app.
                                ApplicationPermission newAppPermissions = applicationService.getDefaultNewApplicationPermissions(savedApp.getId());
                                applicationService.saveApplicationPermissions(newAppPermissions);

                                //save default configuration values for new app.
                                configurationService.setDefaultConfigurationValuesForApplication(savedApp.getId());
                                log.info("Created new owner id: " + savedOwner.getId() + " and new appId: " + savedApp.getId());
                                accountViewModel.setInfoMessage("Successfully created new application.");
                                return new ModelAndView("redirect:/account/application", "accountViewModel", accountViewModel);

                            } else {
                                log.error("Failed to create new app with name" + newApp.getName() + " : for ownerId: " + owner.getId());
                                accountViewModel.setErrorMessage("Failed to create new app.");
                                return getAccountApplication(accountViewModel, modelMap);
                            }
                        } else {
                            log.error("Failed to create new owner for email: " + owner.getEmail());
                            accountViewModel.setErrorMessage("Failed to create new owner for new app.");
                            return getAccountApplication(accountViewModel, modelMap);
                        }
                    }
                }
            } else {
                log.error("Cannot add Disc App to account. Passwords do not match");
                accountViewModel.setErrorMessage("Cannot create new application. Passwords do not match.");
                return getAccountApplication(accountViewModel, modelMap);
            }
        }
        return getAccountApplication(accountViewModel, modelMap);
    }

}
