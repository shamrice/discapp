package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.account.exception.RegistrationCodeRedeemedException;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.model.account.AccountViewModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.*;

@Controller
@Slf4j
public class AccountCreateController extends AccountController {

    @GetMapping(ACCOUNT_CREATE)
    public ModelAndView getCreateAccount(@ModelAttribute AccountViewModel accountViewModel,
                                         ModelMap modelMap) {
        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping(ACCOUNT_CREATE)
    public ModelAndView postCreateAccount(@ModelAttribute AccountViewModel accountViewModel, ModelMap modelMap, HttpServletRequest request) {

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

            //check if email entered is valid.
            if (!EmailValidator.getInstance().isValid(accountViewModel.getEmail())) {
                log.warn("Account creation failed because email: " + accountViewModel.getEmail() + " is not a valid email adddress.");
                accountViewModel.setErrorMessage("Please enter a valid email address.");
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

    @GetMapping(ACCOUNT_CREATE_ACCOUNT_SUCCESS)
    public String getCreateSuccess(ModelMap modelMap) {
        modelMap.addAttribute("status", "Successfully created new user");
        return "account/createAccountSuccess";
    }

    @GetMapping(ACCOUNT_USER_REGISTRATION)
    public ModelAndView getUserRegistration(@RequestParam(name = "email") String email,
                                            @RequestParam(name = "key") String registrationKey,
                                            ModelMap modelMap, HttpServletRequest request) {

        modelMap.addAttribute("adminEmail", configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_ADMIN_ADDRESS, ""));

        try {
            if (discAppUserDetailsService.redeemNewUserRegistrationKey(email, registrationKey)) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                if (user != null) {

                    //if owner id is not null, account was created with wizard and has application
                    //and owner that needs to be enabled as well.
                    if (user.getOwnerId() != null) {
                        Owner owner = accountService.getOwnerById(user.getOwnerId());
                        if (owner != null) {
                            log.info("Enabling new account owner record for email: " + owner.getEmail());
                            owner.setEnabled(true);
                            owner.setModDt(new Date());
                            accountService.saveOwner(owner);

                            String baseUrl = webHelper.getBaseUrl(request);

                            log.info("Enabling new account applications for email: " + user.getEmail() + " :: Owner Id: " + user.getOwnerId());
                            List<Application> applicationList = applicationService.getByOwnerId(user.getOwnerId());
                            for (Application application : applicationList) {
                                application.setEnabled(true);
                                application.setModDt(new Date());
                                applicationService.save(application);

                                //send new application information notification email
                                applicationService.sendNewApplicationInfoEmail(user.getEmail(), application, baseUrl);
                            }
                        }
                    }
                }

                modelMap.addAttribute("success", true);
            } else {
                modelMap.addAttribute("success", false);
            }
        } catch (RegistrationCodeRedeemedException ex) {
            log.error("Registration failed for email: " + email + " and key: " + registrationKey
                    + " :: Error: ", ex.getMessage(), ex);
            modelMap.addAttribute("success", false);
            modelMap.addAttribute("isRedeemed", true);
        }

        return new ModelAndView("account/registration/redeem", "model", modelMap);
    }

}
