package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.service.application.permission.HtmlPermission;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceSecurityViewModel;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceUserSearchViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
public class SecurityMaintenanceController extends MaintenanceController {

    @PostMapping(CONTROLLER_URL_DIRECTORY + "disc-user-search.cgi")
    public ModelAndView postUserSearchView(@RequestParam(name = "id") long appId,
                                           MaintenanceUserSearchViewModel maintenanceUserSearchViewModel,
                                           Model model,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {

        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);

        try {
            //if cancel, return back to security page.
            if (maintenanceUserSearchViewModel.getCancel() != null) {
                return new ModelAndView("redirect:/admin/disc-security.cgi?id=" + appId);
            }

            //search users.
            if (maintenanceUserSearchViewModel.getSearchUsers() != null) {
                String searchTerm = maintenanceUserSearchViewModel.getSearchTerm();

                //add result by email if email searched.
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    if (searchTerm.contains("@")) {
                        DiscAppUser result = discAppUserDetailsService.getByEmail(searchTerm);
                        if (result != null) {
                            if (app.getOwnerId().equals(result.getOwnerId())) {
                                log.info("Cannot add email : " + username + " to appid " + appId + " editors. Is already owner.");
                                maintenanceUserSearchViewModel.setErrorMessage(searchTerm
                                        + " is the owner of this DiscussionApp and already has full administrative privileges.");
                            } else {
                                maintenanceUserSearchViewModel.getSearchResults().add(result);
                            }
                        }
                    } else {
                        List<DiscAppUser> searchResults = discAppUserDetailsService.searchByUsername(searchTerm, true);

                        //remove owners from results
                        searchResults.removeAll(discAppUserDetailsService.getByOwnerId(app.getOwnerId()));

                        //limit results to 10.
                        if (searchResults.size() > 10) {
                            searchResults = searchResults.subList(0, 9);
                        }
                        maintenanceUserSearchViewModel.setSearchResults(searchResults);
                    }
                }
            }

            //add selected accounts
            if (maintenanceUserSearchViewModel.getAddAccounts() != null) {

                List<UserPermission> newUsers = new ArrayList<>();
                List<UserPermission> currentUsers = applicationService.getUserPermissions(app.getId());

                List<Long> currentEditorUserIds = new ArrayList<>();
                for (UserPermission currentEditor : currentUsers) {
                    currentEditorUserIds.add(currentEditor.getDiscAppUser().getId());
                    log.warn("Found existing user: " + currentEditor.toString());
                }

                for (long id : maintenanceUserSearchViewModel.getAddAccountId()) {
                    log.warn("Adding user id: " + id);
                    //don't add editor if already exists.
                    if (!currentEditorUserIds.contains(id)) {

                        DiscAppUser user = discAppUserDetailsService.getByDiscAppUserId(id);
                        if (user != null && user.getIsUserAccount() && !app.getOwnerId().equals(user.getOwnerId())) {
                            UserPermission newUserPermission = new UserPermission();
                            newUserPermission.setApplicationId(app.getId());
                            newUserPermission.setDiscAppUser(user);
                            newUserPermission.setUserPermissions(io.github.shamrice.discapp.service.application.permission.UserPermission.READ + io.github.shamrice.discapp.service.application.permission.UserPermission.REPLY + io.github.shamrice.discapp.service.application.permission.UserPermission.POST);
                            newUserPermission.setIsActive(true);
                            newUserPermission.setCreateDt(new Date());
                            newUserPermission.setModDt(new Date());
                            log.warn("Found new user for this app: " + newUserPermission.toString());
                            newUsers.add(newUserPermission);
                        }

                    } else {
                        //update existing record to active if exists and is currently inactive.
                        for (UserPermission existing : currentUsers) {
                            if (existing.getDiscAppUser().getId().equals(id) && !existing.getIsActive()) {
                                log.warn("Found existing user for this app. " + existing.toString());
                                existing.setUserPermissions(io.github.shamrice.discapp.service.application.permission.UserPermission.READ + io.github.shamrice.discapp.service.application.permission.UserPermission.REPLY + io.github.shamrice.discapp.service.application.permission.UserPermission.POST);
                                existing.setIsActive(true);
                                existing.setModDt(new Date());

                                newUsers.add(existing);
                            }
                        }
                    }
                }

                //save and add.
                if (applicationService.saveUserPermissions(app.getId(), newUsers)) {
                    log.info("Added " + newUsers.size() + " new users to appId: " + appId);
                    return new ModelAndView("redirect:/admin/disc-security.cgi?id=" + appId);
                } else {
                    log.error("Failed to add new users to appId: " + appId);
                    maintenanceUserSearchViewModel.setErrorMessage("Failed to add new users.");
                }
            }

            return new ModelAndView("admin/disc-user-search", "maintenanceUserSearchViewModel", maintenanceUserSearchViewModel);

        } catch (Exception ex) {
            log.error("Error getting maintenance security page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-user-search.cgi")
    public ModelAndView getUserSearchView(@RequestParam(name = "id") long appId,
                                          MaintenanceUserSearchViewModel maintenanceUserSearchViewModel,
                                          Model model,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {

        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);

        return new ModelAndView("admin/disc-user-search", "maintenanceUserSearchViewModel", maintenanceUserSearchViewModel);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "disc-security.cgi")
    public ModelAndView postDiscSecurityView(@RequestParam(name = "id") long appId,
                                             MaintenanceSecurityViewModel maintenanceSecurityViewModel,
                                             Model model,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {

        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);
        try {

            boolean isUserAccount = false;

            DiscAppUser user = discAppUserDetailsService.getByEmail(username);
            if (user != null && user.getIsUserAccount() != null) {
                isUserAccount = user.getIsUserAccount();
            }

            //update owner email
            if (maintenanceSecurityViewModel.getChangeOwnerEmail() != null) {
                if (!isUserAccount) {
                    log.warn("System account: " + username + " attempted to update owner email address but was blocked from doing so.");
                    maintenanceSecurityViewModel.setErrorMessage("Only the account logged in as the owner of this application may update the owner email address.");
                } else {
                    Owner ownerToUpdate = accountService.getOwnerById(app.getOwnerId());
                    String newEmail = inputHelper.sanitizeInput(maintenanceSecurityViewModel.getOwnerEmail());
                    if (!newEmail.trim().isEmpty()) {
                        ownerToUpdate.setEmail(newEmail);
                        ownerToUpdate.setModDt(new Date());
                        try {
                            if (accountService.saveOwner(ownerToUpdate) != null) {
                                log.info("Updated owner: " + ownerToUpdate.toString());
                                maintenanceSecurityViewModel.setOwnerEmailMessage("Email address updated");
                            } else {
                                log.error("Failed to update owners email: " + ownerToUpdate.toString());
                                maintenanceSecurityViewModel.setErrorMessage("Failed to update owner email.");
                            }
                        } catch (Exception ex) {
                            log.error("Error updating owner email address: " + ex.getMessage(), ex);
                            maintenanceSecurityViewModel.setErrorMessage("Owner email address already in use. Please specify a different email.");
                        }
                    } else {
                        maintenanceSecurityViewModel.setErrorMessage("Owner email is either invalid or empty.");
                    }
                }
            }

            //get current permissions, or create new ones if they don't already exist.
            ApplicationPermission appPermission = applicationService.getApplicationPermissions(app.getId());
            if (appPermission == null) {
                appPermission = applicationService.getDefaultNewApplicationPermissions(app.getId());
            }
            appPermission.setModDt(new Date());

            //set security permissions
            if (maintenanceSecurityViewModel.getChangeSecurity() != null) {
                appPermission.setDisplayIpAddress(maintenanceSecurityViewModel.isShowIp());
                appPermission.setBlockBadWords(maintenanceSecurityViewModel.isBlockBadWords());
                appPermission.setBlockSearchEngines(maintenanceSecurityViewModel.isBlockSearch());
                if (applicationService.saveApplicationPermissions(appPermission)) {
                    log.info("Saved updated app permissions: " + appPermission.toString());
                    maintenanceSecurityViewModel.setSecurityMessage("Settings updated");
                } else {
                    log.error("Failed to update security settings: " + appPermission.toString());
                    maintenanceSecurityViewModel.setErrorMessage("Failed to update security settings.");
                }
            }

            //html blocking permissions
            if (maintenanceSecurityViewModel.getChangeHTMLPerms() != null) {
                appPermission.setAllowHtmlPermissions(maintenanceSecurityViewModel.getBlockHtml());
                if (applicationService.saveApplicationPermissions(appPermission)) {
                    log.info("Saved updated app HTML permissions: " + appPermission.toString());
                    maintenanceSecurityViewModel.setHtmlMessage("HTML permissions updated.");
                } else {
                    log.error("Failed to update HTML settings: " + appPermission.toString());
                    maintenanceSecurityViewModel.setErrorMessage("Failed to update HTML permissions.");
                }
            }

            //reg and unreg user posting permissions.
            if (maintenanceSecurityViewModel.getChangeDefaultAccess() != null) {
                appPermission.setUnregisteredUserPermissions(maintenanceSecurityViewModel.getUnregisteredPermissions());
                appPermission.setRegisteredUserPermissions(maintenanceSecurityViewModel.getRegisteredPermissions());
                if (applicationService.saveApplicationPermissions(appPermission)) {
                    log.info("Saved updated app user posting permissions: " + appPermission.toString());
                    maintenanceSecurityViewModel.setPermissionMessage("Permissions Updated");
                } else {
                    log.error("Failed to update user posting permissions: " + appPermission.toString());
                    maintenanceSecurityViewModel.setErrorMessage("Failed to update permissions.");
                }
            }

            //block ip prefix settings
            if (maintenanceSecurityViewModel.getChangeIPs() != null) {

                List<ApplicationIpBlock> applicationIpBlocks = new ArrayList<>();
                String ipPrefixRegex = "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){0,3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])(?:\\.|)$";

                for (int i = 0; i < maintenanceSecurityViewModel.getBlockIpList().length; i++) {
                    String ipPrefix = maintenanceSecurityViewModel.getBlockIpList()[i];
                    if (ipPrefix != null && !ipPrefix.trim().isEmpty() && ipPrefix.matches(ipPrefixRegex)) {
                        applicationIpBlocks.add(buildApplicationIpBlock(app.getId(), ipPrefix));
                    }
                }

                //save
                if (applicationService.saveApplicationBlockIps(app.getId(), applicationIpBlocks)) {
                    log.info("Saved updated ip block prefixes for appId: " + appId);
                    maintenanceSecurityViewModel.setIpMessage("IPs updated");
                } else {
                    log.error("Failed to save new ip block prefixes for appId: " + appId);
                    maintenanceSecurityViewModel.setErrorMessage("Failed to save IP block prefixes.");
                }
            }

            //user permissions setting
            if (maintenanceSecurityViewModel.getChangeUserAccess() != null) {

                List<UserPermission> currentPermissions = applicationService.getUserPermissions(app.getId());
                for (UserPermission userPermission : currentPermissions) {
                    for (UserPermission updatedPerms : maintenanceSecurityViewModel.getUserPermissions()) {
                        //only update matching records when not set to delete.
                        if (!updatedPerms.getUserPermissions().equalsIgnoreCase("delete") && userPermission.getId().equals(updatedPerms.getId())) {
                            userPermission.setUserPermissions(updatedPerms.getUserPermissions());
                            userPermission.setModDt(new Date());
                            log.info("Updating user permissions for id: " + userPermission.getId() + " : for appid: "
                                    + appId + " to: " + userPermission.getUserPermissions());
                            break;
                        }
                    }
                }

                if (applicationService.saveUserPermissions(app.getId(), currentPermissions)) {
                    maintenanceSecurityViewModel.setUserPermissionMessage("Permissions updated.");
                    log.info("Updated user permissions for appId: " + appId);
                } else {
                    log.error("Error saving updated user permissions for appId: " + appId);
                    maintenanceSecurityViewModel.setErrorMessage("Failed to save updated user permissions.");
                }
            }

            //user permissions delete
            if (maintenanceSecurityViewModel.getDeleteUsers() != null) {

                List<UserPermission> currentPermissions = applicationService.getUserPermissions(app.getId());
                for (UserPermission updatedPerms : maintenanceSecurityViewModel.getUserPermissions()) {
                    //if updated perms set to delete for the user...
                    if (updatedPerms.getUserPermissions().equalsIgnoreCase("delete")) {
                        //make sure user is in current application user list before deleting.
                        for (UserPermission userPermission : currentPermissions) {
                            //find match and soft delete (deactivate).
                            if (userPermission.getId().equals(updatedPerms.getId())) {
                                applicationService.setUserPermissionActivation(userPermission.getId(), false);
                                log.info("Deactivated user permissions for id: " + userPermission.getId() + " : for appid: "
                                        + appId + " to: " + userPermission.getUserPermissions());
                                break;
                            }
                        }
                    }
                }
            }

            //redirect to user search page.
            if (maintenanceSecurityViewModel.getSearchUsersForm() != null) {
                return getUserSearchView(appId, new MaintenanceUserSearchViewModel(), model, request, response);
            }

            return getDiscSecurityView(appId, maintenanceSecurityViewModel, model, request, response);

        } catch (Exception ex) {
            log.error("Error getting maintenance security page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-security.cgi")
    public ModelAndView getDiscSecurityView(@RequestParam(name = "id") long appId,
                                            MaintenanceSecurityViewModel maintenanceSecurityViewModel,
                                            Model model,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();

            setCommonModelAttributes(model, app, username);
            maintenanceSecurityViewModel.setApplicationId(app.getId());

            Owner appOwner = accountService.getOwnerById(app.getOwnerId());
            if (appOwner != null) {
                maintenanceSecurityViewModel.setOwnerEmail(appOwner.getEmail());
            }

            String baseUrl = webHelper.getBaseUrl(request);
            maintenanceSecurityViewModel.setEditUrl(baseUrl + "/admin/disc-edit.cgi?id=" + appId);

            ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);
            if (applicationPermission != null) {
                maintenanceSecurityViewModel.setBlockBadWords(applicationPermission.getBlockBadWords());
                maintenanceSecurityViewModel.setBlockSearch(applicationPermission.getBlockSearchEngines());
                maintenanceSecurityViewModel.setShowIp(applicationPermission.getDisplayIpAddress());
                maintenanceSecurityViewModel.setRegisteredPermissions(applicationPermission.getRegisteredUserPermissions());
                maintenanceSecurityViewModel.setUnregisteredPermissions(applicationPermission.getUnregisteredUserPermissions());
                maintenanceSecurityViewModel.setBlockHtml(applicationPermission.getAllowHtmlPermissions());
            } else {
                //if permissions are null. set default permission check boxes.
                maintenanceSecurityViewModel.setUnregisteredPermissions(io.github.shamrice.discapp.service.application.permission.UserPermission.READ + io.github.shamrice.discapp.service.application.permission.UserPermission.REPLY + io.github.shamrice.discapp.service.application.permission.UserPermission.POST);
                maintenanceSecurityViewModel.setRegisteredPermissions(io.github.shamrice.discapp.service.application.permission.UserPermission.READ + io.github.shamrice.discapp.service.application.permission.UserPermission.REPLY + io.github.shamrice.discapp.service.application.permission.UserPermission.POST);
                maintenanceSecurityViewModel.setBlockHtml(HtmlPermission.BLOCK_SUBJECT_SUBMITTER_FIELDS);
                maintenanceSecurityViewModel.setShowIp(true);
            }

            //set values for blocked ips if exist.
            List<ApplicationIpBlock> applicationIpBlocks = applicationService.getBlockedIpPrefixes(app.getId());
            if (applicationIpBlocks != null && applicationIpBlocks.size() > 0) {
                for (int i = 0; i < maintenanceSecurityViewModel.getBlockIpList().length; i++) {
                    if (i < applicationIpBlocks.size()) {
                        maintenanceSecurityViewModel.getBlockIpList()[i] = applicationIpBlocks.get(i).getIpAddressPrefix();
                    } else {
                        maintenanceSecurityViewModel.getBlockIpList()[i] = "";
                    }
                }
            }

            //add editor permissions for active editors.
            maintenanceSecurityViewModel.setUserPermissions(new ArrayList<>());
            List<UserPermission> editors = applicationService.getUserPermissions(app.getId());
            for (UserPermission editor : editors) {
                if (editor.getIsActive()) {
                    maintenanceSecurityViewModel.getUserPermissions().add(editor);
                }
            }

            return new ModelAndView("admin/disc-security", "maintenanceSecurityModel", maintenanceSecurityViewModel);

        } catch (Exception ex) {
            log.error("Error getting maintenance security page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    private ApplicationIpBlock buildApplicationIpBlock(long appId, String ipPrefix) {
        ApplicationIpBlock ipBlock = new ApplicationIpBlock();
        ipBlock.setApplicationId(appId);
        ipBlock.setCreateDt(new Date());
        ipBlock.setModDt(new Date());
        ipBlock.setIpAddressPrefix(ipPrefix);
        return ipBlock;
    }

}
