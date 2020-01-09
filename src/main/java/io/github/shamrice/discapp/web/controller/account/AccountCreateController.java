package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.web.model.account.AccountViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_CREATE;
import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_CREATE_ACCOUNT_SUCCESS;

@Controller
@Slf4j
public class AccountCreateController extends AccountController {

    @GetMapping(ACCOUNT_CREATE)
    public ModelAndView getCreateAccount(@ModelAttribute AccountViewModel accountViewModel,
                                         ModelMap modelMap) {
        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping(ACCOUNT_CREATE)
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

    @GetMapping(ACCOUNT_CREATE_ACCOUNT_SUCCESS)
    public String getCreateSuccess(ModelMap modelMap) {
        modelMap.addAttribute("status", "Successfully created new user");
        return "account/createAccountSuccess";
    }


}
