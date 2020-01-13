package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.model.account.AccountViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.*;
import static io.github.shamrice.discapp.web.define.url.AppUrl.APP_SEARCH_URL;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.MAINTENANCE_PAGE;

@Controller
@Slf4j
public class AccountApplicationController extends AccountController {


    @GetMapping(ACCOUNT_APPLICATION)
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

        //HACK : npe block
        if (accountViewModel == null) {
            accountViewModel = new AccountViewModel();
        }

        return new ModelAndView("account/application/manageApplications", "accountViewModel", accountViewModel);
    }



    @PostMapping(ACCOUNT_MODIFY_APPLICATION)
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



    @GetMapping(ACCOUNT_ADD_APPLICATION)
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

    @PostMapping(ACCOUNT_ADD_APPLICATION)
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
                            return getAccountApplication(accountViewModel, modelMap);
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
                            return getAccountApplication(accountViewModel, modelMap);
                        }

                        if (appName == null || appName.trim().isEmpty()) {
                            log.warn("UserId : " + user.getId() + " : email: " + user.getEmail()
                                    + " attempted to create a new application without a name");
                            accountViewModel.setErrorMessage("Application name is required to create an application.");
                            return getAccountApplication(accountViewModel, modelMap);
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