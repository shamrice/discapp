package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.UserConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.enums.AdminReportFrequency;
import io.github.shamrice.discapp.web.model.account.AccountViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.*;

@Controller
@Slf4j
public class AccountModifyController extends AccountController {

    @GetMapping(value = {CONTROLLER_DIRECTORY_URL, "/account"})
    public ModelAndView getAccount(@ModelAttribute AccountViewModel accountViewModel,
                                   HttpServletRequest request,
                                   ModelMap model) {
        return getAccountModifyAccount(accountViewModel, request, model);
    }

    @GetMapping(ACCOUNT_MODIFY)
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
                                    ENABLED,    //not used in view
                                    ENABLED,    //not used in view
                                    AdminReportFrequency.NEVER.name() // not used in view
                            );
                            moderatingApplications.add(accountApplication);
                        }
                    }
                }
                String baseUrl = webHelper.getBaseUrl(request);
                accountViewModel.setBaseEditorUrl(baseUrl + "/admin/disc-edit.cgi?id=");

                accountViewModel.setModeratingApplications(moderatingApplications);
            }

            //get list of applications with read thread tracking has a value.
            List<UserReadThread> readThreads = userReadThreadService.getAllUserReadThreads(user.getId());
            if (readThreads != null) {
                List<Application> appsWithReadThreads = new ArrayList<>();
                for (UserReadThread readThread : readThreads) {
                    Application app = applicationService.get(readThread.getApplicationId());
                    if (app != null) {
                        appsWithReadThreads.add(app);
                    }
                }
                accountViewModel.setUserReadThreadApplications(appsWithReadThreads);
            }

            //get config value for read tracking
            accountViewModel.setReadTrackingEnabled(configurationService.getUserConfigBooleanValue(user.getId(), UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED, false));

            //time zone configuration
            accountViewModel.setUserTimeZoneEnabled(configurationService.getUserConfigBooleanValue(user.getId(), UserConfigurationProperty.USER_TIMEZONE_ENABLED, false));
            accountViewModel.setSelectedTimeZone(configurationService.getUserConfigStringValue(user.getId(), UserConfigurationProperty.USER_TIMEZONE_LOCATION, "UTC"));
            String[] timezoneIds = TimeZone.getAvailableIDs();
            List<String> timezones = Arrays.asList(timezoneIds);
            accountViewModel.setTimeZones(timezones);
        }

        //NPE safety net.
        if (accountViewModel == null) {
            log.error("Entered into modify account page with null account view model. Setting to empty");
            accountViewModel = new AccountViewModel();
        }

        return new ModelAndView("account/modifyAccount", "accountViewModel", accountViewModel);
    }

    @PostMapping(ACCOUNT_MODIFY_PASSWORD)
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
                        //set password fields back to empty on success.
                        accountViewModel.setPassword("");
                        accountViewModel.setNewPassword("");
                        accountViewModel.setConfirmPassword("");
                    } else {
                        log.error("Failed to update password for user: " + user.getEmail() + " : userId: " + user.getId());
                        accountViewModel.setErrorMessage("Failed to update password.");
                    }
                    return getAccountModify(accountViewModel, request, modelMap);
                }
            }
        }

        return new ModelAndView("redirect:/account/modify");
    }

    @GetMapping(ACCOUNT_MODIFY_ACCOUNT)
    public ModelAndView getAccountModifyAccount(@ModelAttribute AccountViewModel accountViewModel,
                                                HttpServletRequest request,
                                                ModelMap modelMap) {
        return getAccountModify(accountViewModel, request, modelMap);
    }

    @PostMapping(ACCOUNT_MODIFY_ACCOUNT)
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



    @PostMapping(ACCOUNT_MODIFY_OWNER)
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

                        owner.setFirstName(firstName);
                        owner.setLastName(lastName);
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


    @GetMapping(ACCOUNT_MODIFY_READ_RESET)
    public ModelAndView getAccountModifyReadReset(@RequestParam(name = "appId") long appId) {
        Long userId = accountHelper.getLoggedInDiscAppUserId();
        if (userId != null) {
            userReadThreadService.resetReadThreads(appId, userId);
        }

        return new ModelAndView("redirect:/account/modify#user_read_threads_banner");
    }

    @GetMapping(ACCOUNT_MODIFY_READ_STATUS)
    public ModelAndView getAccountModifyReadReset(@PathVariable(name = "status") String status) {
        Long userId = accountHelper.getLoggedInDiscAppUserId();
        if (userId != null) {

            UserConfiguration readTracking = configurationService.getUserConfiguration(userId, UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED.getPropName());
            if (readTracking == null) {
                readTracking = new UserConfiguration();
                readTracking.setDiscappUserId(userId);
                readTracking.setName(UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED.getPropName());
                readTracking.setCreateDt(new Date());
            } else {
                readTracking.setModDt(new Date());
            }

            if ("enable".equalsIgnoreCase(status)) {
                readTracking.setValue("true");
            } else {
                readTracking.setValue("false");
            }

            if (configurationService.saveUserConfiguration(UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED, readTracking)) {
                log.info("Updated userId: " + userId + " configuration value for " + UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED.getPropName() + " to true.");
            } else {
                log.info("Updated userId: " + userId + " configuration value for " + UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED.getPropName() + " to false.");
            }

        }

        return new ModelAndView("redirect:/account/modify#user_read_threads_banner");
    }

    @PostMapping(ACCOUNT_MODIFY_LOCALE)
    public ModelAndView postLocaleModify(@ModelAttribute AccountViewModel accountViewModel,
                                        HttpServletRequest request,
                                        ModelMap modelMap) {

        if (accountViewModel != null) {
            String email = accountHelper.getLoggedInEmail();
            if (email != null && !email.trim().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                if (user != null) {

                    UserConfiguration userTimeZone = configurationService.getUserConfiguration(user.getId(), UserConfigurationProperty.USER_TIMEZONE_LOCATION.getPropName());
                    if (userTimeZone == null) {
                        userTimeZone = new UserConfiguration();
                        userTimeZone.setDiscappUserId(user.getId());
                        userTimeZone.setName(UserConfigurationProperty.USER_TIMEZONE_LOCATION.getPropName());
                    }
                    userTimeZone.setValue(accountViewModel.getSelectedTimeZone());
                    configurationService.saveUserConfiguration(UserConfigurationProperty.USER_TIMEZONE_LOCATION, userTimeZone);

                    UserConfiguration userTimeZoneEnabled = configurationService.getUserConfiguration(user.getId(), UserConfigurationProperty.USER_TIMEZONE_ENABLED.getPropName());
                    if (userTimeZoneEnabled == null) {
                        userTimeZoneEnabled = new UserConfiguration();
                        userTimeZoneEnabled.setDiscappUserId(user.getId());
                        userTimeZoneEnabled.setName(UserConfigurationProperty.USER_TIMEZONE_ENABLED.getPropName());
                    }
                    userTimeZoneEnabled.setValue(String.valueOf(accountViewModel.isUserTimeZoneEnabled()));
                    configurationService.saveUserConfiguration(UserConfigurationProperty.USER_TIMEZONE_ENABLED, userTimeZoneEnabled);

                    accountViewModel.setInfoMessage("User time zone settings updated.");
                    log.info("Updated time zone configuration for user id: " + user.getId() + " :: time zone enabled: "
                            + userTimeZoneEnabled.getValue() + " :: time zone location: " + userTimeZone.getValue());
                }
            }
        }

        return getAccountModify(accountViewModel, request, modelMap);
    }

}
