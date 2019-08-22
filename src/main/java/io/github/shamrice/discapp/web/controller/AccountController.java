package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.model.AccountViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Controller
@Slf4j
public class AccountController {

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @GetMapping("/account/create")
    public ModelAndView getCreateAccount(@ModelAttribute AccountViewModel accountViewModel,
                                         @RequestParam(required = false) String redirect,
                                         ModelMap modelMap) {

        if (redirect == null || redirect.isEmpty()) {
            redirect = "/login";
        }

        modelMap.addAttribute("redirectUrl", redirect);
        return new ModelAndView("account/createAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping("/account/create")
    public ModelAndView postCreateAccount(@ModelAttribute AccountViewModel accountViewModel, ModelMap modelMap) {

        if (accountViewModel != null) {
            log.info("Attempting to create new account with username: " + accountViewModel.getUsername());


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

            DiscAppUser newUser = new DiscAppUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setIsAdmin(accountViewModel.isAdmin());
            newUser.setEmail(accountViewModel.getEmail());
            newUser.setShowEmail(accountViewModel.isShowEmail());
            newUser.setEnabled(accountViewModel.isEnabled());
            newUser.setOwnerId(accountViewModel.getOwnerId());
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

    @GetMapping("/account/modify")
    public ModelAndView getAccountModify(@ModelAttribute AccountViewModel accountViewModel,
                                         @RequestParam(required = false) String redirect,
                                         ModelMap modelMap) {

        //todo : not too happy with this flow for the redirect....
        if (accountViewModel != null && accountViewModel.getRedirect() != null && !accountViewModel.getRedirect().isEmpty()) {
            redirect = accountViewModel.getRedirect();
        }

        if (redirect == null || redirect.isEmpty()) {
            redirect = "/logout";
        }

        if (accountViewModel != null && (accountViewModel.getRedirect() == null || accountViewModel.getRedirect().isEmpty())) {
            accountViewModel.setRedirect(redirect);
        }

        modelMap.addAttribute("redirectUrl", redirect);

        String email = accountHelper.getLoggedInEmail();

        if (accountViewModel != null && email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);

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

                    //TODO : modify account screen should be able to edit all apps owned by the user.
                    List<Application> apps = applicationService.getByOwnerId(owner.getId());
                    if (apps != null && apps.size() > 0) {
                        Application app = apps.get(0); //just get first one for now

                        accountViewModel.setApplicationId(app.getId());
                        accountViewModel.setApplicationName(app.getName());
                    }
                }

            }
        }

        return new ModelAndView("account/modifyAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping("/account/modify/account")
    public ModelAndView postAccountModify(@ModelAttribute AccountViewModel accountViewModel,
                                          @RequestParam(required = false) String redirect,
                                          ModelMap modelMap) {

        if (accountViewModel != null) {

            accountViewModel.setRedirect(redirect);

            String password = inputHelper.sanitizeInput(accountViewModel.getPassword());
            String username = inputHelper.sanitizeInput(accountViewModel.getUsername());

            if (accountViewModel.getPassword().equals(accountViewModel.getConfirmPassword())
                    && accountViewModel.getPassword() != null && !accountViewModel.getPassword().isEmpty()
                    && accountViewModel.getConfirmPassword() != null && !accountViewModel.getConfirmPassword().isEmpty()
                    && password.equals(accountViewModel.getPassword())) {

                String email = accountHelper.getLoggedInEmail();

                DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                if (user != null) {
                    if (username != null && !username.isEmpty()) {
                        user.setUsername(username);
                    }
                    user.setShowEmail(accountViewModel.isShowEmail());
                    user.setModDt(new Date());
                    user.setPassword(password);

                    if (!discAppUserDetailsService.saveDiscAppUser(user)) {
                        log.error("Failed to update user : " + email + ". Changes will not be saved.");
                        accountViewModel.setErrorMessage("Failed to update user.");
                    } else {
                        accountViewModel.setErrorMessage("Successfully updated user information.");
                        log.info("User " + email + " account information was updated.");

                    }
                }
            } else {
                log.error("Passwords do not match. Cannot update account information.");
                accountViewModel.setErrorMessage("Passwords don't match. Cannot update account information.");
            }
        }

        return getAccountModify(accountViewModel, redirect, modelMap);

    }


    @PostMapping("/account/modify/application")
    public ModelAndView postApplicationModify(@ModelAttribute AccountViewModel accountViewModel,
                                              @RequestParam(required = false) String redirect,
                                              ModelMap modelMap) {

        if (accountViewModel != null) {

            accountViewModel.setRedirect(redirect);

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

                                if (applicationService.save(app) != null) {
                                    log.info("Application id: " + app.getId() + " has been updated.");
                                    accountViewModel.setErrorMessage("Application name has been updated.");
                                } else {
                                    log.error("Unable to save application changes for appId:" + app.getId());
                                    accountViewModel.setErrorMessage("Failed to update application name.");
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


        return getAccountModify(accountViewModel, redirect, modelMap);
    }


    @PostMapping("/account/modify/owner")
    public ModelAndView postOwnerModify(@ModelAttribute AccountViewModel accountViewModel,
                                        @RequestParam(required = false) String redirect,
                                        ModelMap modelMap) {

        if (accountViewModel != null) {

            accountViewModel.setRedirect(redirect);

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
                            accountViewModel.setErrorMessage("Owner information has been updated.");
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

        return getAccountModify(accountViewModel, redirect, modelMap);
    }

    @PostMapping("/account/add/application")
    public ModelAndView postAddApplication(@ModelAttribute AccountViewModel accountViewModel,
                                           @RequestParam(required = false) String redirect,
                                           ModelMap modelMap) {

        if (accountViewModel != null) {

            accountViewModel.setRedirect(redirect);

            String password = inputHelper.sanitizeInput(accountViewModel.getPassword());

            if (accountViewModel.getPassword().equals(accountViewModel.getConfirmPassword())
                    && accountViewModel.getPassword() != null && !accountViewModel.getPassword().isEmpty()
                    && accountViewModel.getConfirmPassword() != null && !accountViewModel.getConfirmPassword().isEmpty()
                    && password.equals(accountViewModel.getPassword())) {


                String email = accountHelper.getLoggedInEmail();

                if (email != null && !email.trim().isEmpty()) {

                    DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                    if (user != null) {

                        String ownerFirstName = inputHelper.sanitizeInput(accountViewModel.getOwnerFirstName());
                        String ownerLastName = inputHelper.sanitizeInput(accountViewModel.getOwnerLastName());
                        String ownerPhone = inputHelper.sanitizeInput(accountViewModel.getOwnerPhone());

                        Owner newOwner = new Owner();
                        newOwner.setFirstName(ownerFirstName);
                        newOwner.setLastName(ownerLastName);
                        newOwner.setEmail(user.getEmail()); //use same user email
                        newOwner.setPhone(ownerPhone);
                        newOwner.setEnabled(true);
                        newOwner.setCreateDt(new Date());
                        newOwner.setModDt(new Date());

                        Owner savedOwner = accountService.saveOwner(newOwner);
                        if (savedOwner != null) {

                            String appName = inputHelper.sanitizeInput(accountViewModel.getApplicationName());

                            Application newApp = new Application();
                            newApp.setName(appName);
                            newApp.setOwnerId(savedOwner.getId());
                            newApp.setCreateDt(new Date());
                            newApp.setModDt(new Date());

                            Application savedApp = applicationService.save(newApp);

                            if (savedApp != null) {
                                user.setOwnerId(newOwner.getId());
                                user.setPassword(password);
                                user.setIsAdmin(true);
                                discAppUserDetailsService.saveDiscAppUser(user);

                                //save default configuration values for new app.
                                configurationService.setDefaultConfigurationValuesForApplication(savedApp.getId());
                                accountViewModel.setRedirect("/account/modify");

                                log.info("Created new owner id: " + savedOwner.getId() + " and new appId: " + savedApp.getId());
                                accountViewModel.setErrorMessage("Successfully created new owner and application for user.");

                            } else {
                                log.error("Failed to create new app with name" + newApp.getName() + " : for ownerId: " + newOwner.getId());
                                accountViewModel.setErrorMessage("Failed to create new app.");
                            }
                        } else {
                            log.error("Failed to create new owner for email: " + newOwner.getEmail());
                            accountViewModel.setErrorMessage("Failed to create new owner for new app.");
                        }
                    }
                }
            } else {
                log.error("Cannot create a new account. passwords do not match");
                accountViewModel.setErrorMessage("Cannot create new application. Passwords do not match.");
            }
        }


        return getAccountModify(accountViewModel, redirect, modelMap);
    }
}
