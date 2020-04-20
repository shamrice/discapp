package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.web.model.account.AccountViewModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.*;
import static io.github.shamrice.discapp.web.define.url.AppUrl.APP_SEARCH_URL;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.MAINTENANCE_PAGE;

@Controller
@Slf4j
public class AccountWizardController extends AccountController {

    @GetMapping(ACCOUNT_CREATE_WIZARD)
    public ModelAndView getCreateAccountWizard(@ModelAttribute AccountViewModel accountViewModel,
                                         ModelMap modelMap) {
        return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
    }

    @PostMapping(ACCOUNT_CREATE_WIZARD_ADD)
    public ModelAndView postCreateAccountWizardAdd(@ModelAttribute AccountViewModel accountViewModel, ModelMap modelMap, HttpServletRequest request) {

        //todo : this is a huge chunk of code pieced together from other controllers. It should all live somewhere central.

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

            if (username.isEmpty()) {
                accountViewModel.setErrorMessage("Username is either invalid or empty. Please select a username.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (!accountViewModel.getPassword().equals(accountViewModel.getConfirmPassword())
                    || !accountViewModel.getPassword().equals(password)) {

                accountViewModel.setErrorMessage("Passwords do not match or contain invalid characters.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (password.length() < 8) {
                accountViewModel.setErrorMessage("Passwords must be at least eight characters in length.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            //let user know if username has already been taken before attempting to create new user.
            if (discAppUserDetailsService.getByUsername(username) != null) {
                log.warn("Account creation failed for user: " + accountViewModel.getEmail()
                        + " because username is already taken. Username: " + username);
                accountViewModel.setErrorMessage("Display name: " + username + " has already been taken. Please specify a different disc app display name.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            //let user know if email address has already been taken before attempting to create
            if (discAppUserDetailsService.getByEmail(accountViewModel.getEmail()) != null) {
                log.warn("Account creation failed for user: " + accountViewModel.getEmail()
                        + " because email address is already taken.");
                accountViewModel.setErrorMessage("Email: " + accountViewModel.getEmail() + " has already been taken. Please specify a different email address.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            //check if email entered is valid.
            if (!EmailValidator.getInstance().isValid(accountViewModel.getEmail())) {
                log.warn("Account creation failed because email: " + accountViewModel.getEmail() + " is not a valid email adddress.");
                accountViewModel.setErrorMessage("Please enter a valid email address.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }


            String ownerFirstName = inputHelper.sanitizeInput(accountViewModel.getOwnerFirstName());
            String ownerLastName = inputHelper.sanitizeInput(accountViewModel.getOwnerLastName());
            String appName = inputHelper.sanitizeInput(accountViewModel.getApplicationName());


            if ((ownerFirstName == null || ownerFirstName.trim().isEmpty())
                    || (ownerLastName == null || ownerLastName.trim().isEmpty())) {
                log.warn("New account: " + accountViewModel.getEmail()
                        + " attempted to create a new owner without a first or last name.");
                accountViewModel.setErrorMessage("Owner first name and last name are required to create an application.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (appName == null || appName.trim().isEmpty()) {
                log.warn("New account: " + accountViewModel.getEmail()
                        + " attempted to create a new application without an application name");
                accountViewModel.setErrorMessage("Application name is required to create an application.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (accountViewModel.getApplicationAdminPassword() == null || accountViewModel.getApplicationAdminPassword().isEmpty()) {
                accountViewModel.setErrorMessage("Application admin password is required to create an application.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (accountViewModel.getConfirmApplicationAdminPassword() == null || accountViewModel.getConfirmApplicationAdminPassword().isEmpty()) {
                accountViewModel.setErrorMessage("Application admin password confirmation is required to create an application.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }

            if (!accountViewModel.getApplicationAdminPassword().equals(accountViewModel.getConfirmApplicationAdminPassword())) {
                accountViewModel.setErrorMessage("Application admin password and confirmation do not match.");
                return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
            }


            //passed checks. create user.
            DiscAppUser newUser = new DiscAppUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setEmail(accountViewModel.getEmail());
            newUser.setShowEmail(accountViewModel.isShowEmail());
            newUser.setEnabled(accountViewModel.isEnabled());
            newUser.setOwnerId(null);
            newUser.setIsAdmin(true);
            newUser.setEnabled(false);
            newUser.setIsUserAccount(true);
            newUser.setModDt(new Date());
            newUser.setCreateDt(new Date());

            if (discAppUserDetailsService.saveDiscAppUser(newUser)) {

                DiscAppUser newCreatedUser = discAppUserDetailsService.getByEmail(newUser.getEmail());
                if (newCreatedUser == null) {
                    log.error("Failed to find new created user: " + newUser.getEmail());
                    accountViewModel.setErrorMessage("An error has occurred.");
                    return new ModelAndView("account/createAccountWizard", "accountViewModel", accountViewModel);
                }

                Owner newOwner = new Owner();
                newOwner.setEmail(newCreatedUser.getEmail());
                newOwner.setEnabled(false);
                newOwner.setFirstName(accountViewModel.getOwnerFirstName());
                newOwner.setLastName(accountViewModel.getOwnerLastName());
                newOwner.setCreateDt(new Date());
                newOwner.setModDt(new Date());

                Owner savedOwner = accountService.saveOwner(newOwner);

                //update new user with new owner info
                discAppUserDetailsService.updateOwnerInformation(newCreatedUser.getId(), savedOwner.getId(), true);

                //create new application
                Application newApplication = new Application();
                newApplication.setName(accountViewModel.getApplicationName());
                newApplication.setDeleted(false);
                newApplication.setEnabled(false);
                newApplication.setOwnerId(newOwner.getId());
                newApplication.setSearchable(true);
                newApplication.setCreateDt(new Date());
                newApplication.setModDt(new Date());

                Application savedApp = applicationService.save(newApplication);

                if (savedApp != null) {

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
                    if (!discAppUserDetailsService.saveDiscAppUser(newAdminAccount)) {
                        log.error("Failed to create new admin user for new disc app: " + savedApp.getId());
                    }

                    //save default permission values for new app.
                    ApplicationPermission newAppPermissions = applicationService.getDefaultNewApplicationPermissions(savedApp.getId());
                    applicationService.saveApplicationPermissions(newAppPermissions);

                    //save default configuration values for new app.
                    configurationService.setDefaultConfigurationValuesForApplication(savedApp.getId());
                    log.info("Created new owner id: " + savedOwner.getId() + " and new appId: " + savedApp.getId());
                    accountViewModel.setInfoMessage("Successfully created new application.");

                } else {
                    log.warn("Something went wrong saving new application.");
                }

                //send registration email to new user.
                discAppUserDetailsService.createNewUserRegistrationRequest(newUser.getEmail(), webHelper.getBaseUrl(request));
                log.info("Successfully created new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());

                return new ModelAndView("account/createAccountSuccess");
            } else {
                log.error("Failed to create new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());
                accountViewModel.setErrorMessage("Failed to create new account for user: " + accountViewModel.getEmail());
            }
        }

        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);

    }

}
