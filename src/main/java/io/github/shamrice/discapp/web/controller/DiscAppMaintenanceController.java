package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.data.ApplicationExportService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.data.ApplicationImportService;
import io.github.shamrice.discapp.service.application.permission.HtmlPermission;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.stats.StatisticsService;
import io.github.shamrice.discapp.service.storage.FileSystemStorageService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.define.url.MaintenanceUrl;
import io.github.shamrice.discapp.web.model.*;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.List;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.*;

@Controller
@Slf4j
public class DiscAppMaintenanceController {

    public static final String CONTROLLER_URL_DIRECTORY = MaintenanceUrl.CONTROLLER_DIRECTORY_URL;

    private static final String THREAD_TAB = "threads";
    private static final String DATE_TAB = "date";
    private static final String SEARCH_TAB = "search";
    private static final String POST_TAB = "post";
    private static final String UNAPPROVED_TAB = "unapproved";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    private ApplicationExportService applicationExportService;

    @Autowired
    private ApplicationImportService applicationImportService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private WebHelper webHelper;

    @Autowired
    private DiscAppController discAppController;

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
            //update owner email
            if (maintenanceSecurityViewModel.getChangeOwnerEmail() != null) {
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

    @GetMapping(CONTROLLER_URL_DIRECTORY + "data/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadImportFile(@RequestParam(name = "id") long appId) {

        try {
            Application app = applicationService.get(appId);

            ImportData importData = applicationImportService.getImportData(app.getId());

            if (importData != null) {
                Resource file = new ByteArrayResource(importData.getImportData());
                return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + importData.getImportName() + "\"")
                        .body(file);
            }

        } catch (Exception ex) {
            log.error("Error downloading import for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "data/export")
    @ResponseBody
    public ResponseEntity<Resource> postExportFile(@RequestParam(name = "id") long appId,
                                                   MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                                   Model model,
                                                   HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();

            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());

            String filename = applicationExportService.generateExportForApplication(app.getId());
            Resource file = fileSystemStorageService.loadAsResource(filename);
            if (file != null) {
                log.info("Generated export file for appId: " + app.getId());
                return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            } else {
                log.warn("Failed to generate export file for appId: " + app.getId());
                return ResponseEntity.noContent().build();
            }

        } catch (Exception ex) {
            log.error("Error posting maintenance export for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "data/import")
    public ModelAndView postImportFile(@RequestParam(name = "id") long appId,
                                       @RequestParam("uploadSourceFile") MultipartFile uploadSourceFile,
                                       MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                       Model model,
                                       HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());
            String newFilename = "disc_" + app.getId() + ".sql";

            if (applicationImportService.saveImportData(app.getId(), newFilename, uploadSourceFile.getBytes())) {
                log.info("Import for appId: " + app.getId() + " saved successfully to import table.");
                maintenanceImportExportViewModel.setInfoMessage(
                        "Disc App import file successfully uploaded. You will receive an email at your account email address " +
                                "when the import is completed.");

            } else {
                maintenanceImportExportViewModel.setInfoMessage("Failed to upload Disc App import file. Please try again.");
            }

            return getImportExportView(app.getId(), maintenanceImportExportViewModel, model, response);

        } catch (Exception ex) {
            log.error("Error posting maintenance import for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-import-export.cgi")
    public ModelAndView getImportExportView(@RequestParam(name = "id") long appId,
                                            MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                            Model model,
                                            HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());
            return new ModelAndView("admin/disc-import-export", "maintenanceImportExportViewModel", maintenanceImportExportViewModel);

        } catch (Exception ex) {
            log.error("Error getting maintenance import export page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }


    @GetMapping(CONTROLLER_URL_DIRECTORY + "permission-denied")
    public ModelAndView getPermissionDeniedView(@RequestParam(name = "id") long appId,
                                                HttpServletResponse response,
                                                Model model) {
        model.addAttribute(APP_NAME, "");
        model.addAttribute(APP_ID, appId);
        String username = accountHelper.getLoggedInEmail();
        model.addAttribute(USERNAME, username);

        return new ModelAndView("admin/permissionDenied");
    }

    @GetMapping(MAINTENANCE_PAGE)
    public ModelAndView getDiscMaintenanceView(@RequestParam(name = "id") long appId,
                                               Model model,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            return new ModelAndView("admin/disc-maint");

        } catch (Exception ex) {
            log.error("Error getting maintenance page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-toolbar.cgi")
    public ModelAndView getDiscToolbarView(@RequestParam(name = "id") long appId,
                                           Model model,
                                           HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            return new ModelAndView("admin/disc-toolbar");

        } catch (Exception ex) {
            log.error("Error getting maintenance toolbar page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "disc-widget-maint.cgi")
    public ModelAndView postDiscMainWidgetView(@RequestParam(name = "id") long appId,
                                               MaintenanceWidgetViewModel maintenanceWidgetViewModel,
                                               Model model,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //submit changes.
            if (maintenanceWidgetViewModel.getSubmitChanges() != null && !maintenanceWidgetViewModel.getSubmitChanges().isEmpty()) {
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, String.valueOf(maintenanceWidgetViewModel.isShowAuthor()).toLowerCase());
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, String.valueOf(maintenanceWidgetViewModel.isShowDate()).toLowerCase());
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, String.valueOf(maintenanceWidgetViewModel.isShowStyleSheet()).toLowerCase());

                int width = 20;
                try {
                    width = Integer.parseInt(maintenanceWidgetViewModel.getWidgetWidth());
                } catch (NumberFormatException widthEx) {
                    log.warn("Invalid widget width for appId: " + appId
                            + " : value: " + maintenanceWidgetViewModel.getWidgetWidth() + " :: using default: "
                            + width + " :: " + widthEx.getMessage());
                }
                maintenanceWidgetViewModel.setWidgetWidth(String.valueOf(width));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH, String.valueOf(width));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, maintenanceWidgetViewModel.getWidgetWidthUnit());

                int height = 18;
                try {
                    height = Integer.parseInt(maintenanceWidgetViewModel.getWidgetHeight());
                } catch (NumberFormatException heightEx) {
                    log.warn("Invalid widget height for appId: " + appId
                            + " : value: " + maintenanceWidgetViewModel.getWidgetHeight() + " :: using default: "
                            + height + " :: " + heightEx.getMessage());
                }
                maintenanceWidgetViewModel.setWidgetHeight(String.valueOf(height));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, String.valueOf(height));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, maintenanceWidgetViewModel.getWidgetHeightUnit());
            } else {
                //reset to default values.
                //todo : probably should get these values from somewhere in the config service or something...
                String width = "20";
                String widthUnit = "em";
                String height = "18";
                String heightUnit = "em";
                String showAuthor = "true";
                String showDate = "false";
                String useStyleSheet = "true";

                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, showAuthor);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, showDate);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, useStyleSheet);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH, width);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, widthUnit);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, height);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, heightUnit);

                maintenanceWidgetViewModel.setWidgetWidth(width);
                maintenanceWidgetViewModel.setWidgetWidthUnit(widthUnit);
                maintenanceWidgetViewModel.setWidgetHeight(height);
                maintenanceWidgetViewModel.setWidgetHeightUnit(heightUnit);
                maintenanceWidgetViewModel.setShowAuthor(Boolean.parseBoolean(showAuthor));
                maintenanceWidgetViewModel.setShowDate(Boolean.parseBoolean(showDate));
                maintenanceWidgetViewModel.setShowStyleSheet(Boolean.parseBoolean(useStyleSheet));
            }

        } catch (Exception ex) {
            log.error("Error saving widget settings for appId: " + appId + " :: " + ex.getMessage(), ex);
            maintenanceWidgetViewModel.setInfoMessage("Unable to save widget settings. Please try again.");
        }

        return getDiscMaintWidgetView(appId, maintenanceWidgetViewModel, model, request, response);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-widget-maint.cgi")
    public ModelAndView getDiscMaintWidgetView(@RequestParam(name = "id") long appId,
                                               MaintenanceWidgetViewModel maintenanceWidgetViewModel,
                                               Model model,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceWidgetViewModel.setApplicationId(app.getId());

            boolean showAuthor = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, true);
            boolean showDate = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, false);
            boolean useStyleSheet = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, true);
            String width = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_WIDTH, "20");
            String widthUnit = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, "em");
            String height = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, "18");
            String heightUnit = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, "em");

            maintenanceWidgetViewModel.setShowAuthor(showAuthor);
            maintenanceWidgetViewModel.setShowDate(showDate);
            maintenanceWidgetViewModel.setShowStyleSheet(useStyleSheet);
            maintenanceWidgetViewModel.setWidgetWidth(width);
            maintenanceWidgetViewModel.setWidgetWidthUnit(widthUnit);
            maintenanceWidgetViewModel.setWidgetHeight(height);
            maintenanceWidgetViewModel.setWidgetHeightUnit(heightUnit);

            String heightUnitForCode = maintenanceWidgetViewModel.getWidgetHeightUnit();
            String widthUnitForCode = maintenanceWidgetViewModel.getWidgetWidthUnit();

            if (heightUnitForCode.equalsIgnoreCase("percent")) {
                heightUnitForCode = "%";
            }

            if (widthUnitForCode.equalsIgnoreCase("percent")) {
                widthUnitForCode = "%";
            }

            String baseUrl = webHelper.getBaseUrl(request);

            maintenanceWidgetViewModel.setCodeHtml(
                    getWidgetHtml(
                            Integer.parseInt(maintenanceWidgetViewModel.getWidgetWidth()),
                            widthUnitForCode,
                            Integer.parseInt(maintenanceWidgetViewModel.getWidgetHeight()),
                            heightUnitForCode,
                            app.getId(),
                            baseUrl
                    )
            );

        } catch (Exception ex) {
            log.error("Error getting widget maintenance page for : " + appId + " :: " + ex.getMessage(), ex);
            maintenanceWidgetViewModel.setInfoMessage("An unexpected error has occurred. Please try again.");
        }

        return new ModelAndView("admin/disc-widget-maint", "maintenanceWidgetViewModel", maintenanceWidgetViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-locale.cgi")
    public ModelAndView getDiscLocaleView(@RequestParam(name = "id") long appId,
                                          MaintenanceLocaleViewModel maintenanceLocaleViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceLocaleViewModel.setApplicationId(app.getId());

            //time and date config
            String timezone = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");
            maintenanceLocaleViewModel.setSelectedTimezone(timezone);

            String dateFormat = configurationService.getStringValue(appId, ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");
            maintenanceLocaleViewModel.setDateFormat(dateFormat);

            String[] timezoneIds = TimeZone.getAvailableIDs();
            List<String> timezones = Arrays.asList(timezoneIds);
            maintenanceLocaleViewModel.setTimezones(timezones);

        } catch (Exception ex) {
            log.error("Error getting locale admin view: " + ex.getMessage(), ex);
            maintenanceLocaleViewModel.setInfoMessage("An error has occurred. Please try again.");
        }

        return new ModelAndView("admin/disc-locale", "maintenanceLocaleViewModel", maintenanceLocaleViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-stats.cgi")
    public ModelAndView getDiscStatsView(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "selectedStatsId", required = false) Long statsId,
                                         MaintenanceStatsViewModel maintenanceStatsViewModel,
                                         Model model,
                                         HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            long totalPageViews = 0L;
            long totalUniqueIps = 0L;
            float totalUniqueIpsPerDay = 0;
            int numDays = 30;

            String whoIsUrl = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.WHOIS_URL, "https://www.whois.com/whois/");
            maintenanceStatsViewModel.setWhoIsUrl(whoIsUrl);

            List<Stats> pastMonthStats = statisticsService.getLatestStatsForApp(app.getId(), numDays);

            //if there's less than 30 days of data, do calculations on what we actually have.
            if (pastMonthStats.size() < numDays) {
                numDays = pastMonthStats.size();
            }

            List<MaintenanceStatsViewModel.StatView> statViews = new ArrayList<>();
            for (Stats dayStat : pastMonthStats) {
                MaintenanceStatsViewModel.StatView statView = new MaintenanceStatsViewModel.StatView(
                        dayStat.getStatDate(),
                        dayStat.getId(),
                        dayStat.getUniqueIps(),
                        dayStat.getPageViews()
                );
                statViews.add(statView);

                totalPageViews += dayStat.getPageViews();
                totalUniqueIps += dayStat.getUniqueIps();
                totalUniqueIpsPerDay += statView.getPagesPerIp();
            }

            maintenanceStatsViewModel.setApplicationId(app.getId());
            maintenanceStatsViewModel.setStatViews(statViews);
            maintenanceStatsViewModel.setTotalPageViews(totalPageViews);
            maintenanceStatsViewModel.setAveragePageViews(totalPageViews / numDays);
            maintenanceStatsViewModel.setAverageUniqueIps(totalUniqueIps / numDays);
            maintenanceStatsViewModel.setAveragePagesPerIp(totalUniqueIpsPerDay / numDays);

            if (statsId != null && statsId > 0L) {
                Stats selectedStats = statisticsService.getStats(statsId);
                if (selectedStats != null && selectedStats.getApplicationId().equals(app.getId())) {

                    List<StatsUniqueIps> daysUniqueIps = statisticsService.getUniqueIpsForStatsId(selectedStats.getId());

                    maintenanceStatsViewModel.setSelectedStatId(statsId);
                    maintenanceStatsViewModel.setSelectedDate(selectedStats.getStatDate());
                    maintenanceStatsViewModel.setUniqueIps(daysUniqueIps);
                } else {
                    maintenanceStatsViewModel.setInfoMessage("Error retrieving statistics.");
                }
            }
        } catch (Exception ex) {
            log.error("Error getting stats for appId: " + appId + " :: " + ex.getMessage(), ex);
            maintenanceStatsViewModel.setInfoMessage("Error retrieving statistics.");
        }

        return new ModelAndView("admin/disc-stats", "maintenanceStatsViewModel", maintenanceStatsViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-info.cgi")
    public ModelAndView getDiscInfoView(@RequestParam(name = "id") long appId,
                                        Model model,
                                        HttpServletResponse response,
                                        HttpServletRequest request) {
        String baseUrl = webHelper.getBaseUrl(request);
        model.addAttribute(APP_URL, baseUrl + "/indices/" + appId);
        //todo : indices string should be from static final property

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            model.addAttribute(IS_ADMIN, true);

        } catch (Exception ex) {
            log.error("Failed to display landing page for maintenance for appid: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("admin/disc-info");
    }

    @PostMapping(THREADS_EDIT_PAGE)
    public ModelAndView postDiscEditView(HttpServletRequest request,
                                         @RequestParam(name = "id") long appId,
                                         @RequestParam(name = "tab", required = false) String currentTab,
                                         @RequestParam(name = "pagemark", required = false) Long pageMark,
                                         @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                         Model model,
                                         HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //delete threads
            if (maintenanceThreadViewModel.getDeleteArticles() != null && !maintenanceThreadViewModel.getDeleteArticles().isEmpty()) {

                boolean deleteThreadsSuccess = true;

                //delete from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), false)) {
                        log.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        deleteThreadsSuccess = false;
                    }
                } else {
                    //delete from thread view or search view with checkboxes.
                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.deleteThread(app.getId(), threadId, false)) {
                                log.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId());
                                deleteThreadsSuccess = false;
                            }
                        }
                    }
                }
                //set message for user
                if (deleteThreadsSuccess) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully deleted messages.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to delete messages.");
                }
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //delete threads and replies
            if (maintenanceThreadViewModel.getDeleteArticlesAndReplies() != null && !maintenanceThreadViewModel.getDeleteArticlesAndReplies().isEmpty()) {

                boolean deleteThreadsAndRepliesSuccess = true;

                //delete from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), true)) {
                        log.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId() + " and replies.");
                        deleteThreadsAndRepliesSuccess = false;
                    }
                } else {
                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.deleteThread(app.getId(), threadId, true)) {
                                log.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId() + " and replies.");
                                deleteThreadsAndRepliesSuccess = false;
                            }
                        }
                    }
                }

                if (deleteThreadsAndRepliesSuccess) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully deleted messages and replies.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to delete messages and replies.");
                }
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //make this a top level article
            if (maintenanceThreadViewModel.getMakeThisTopLevelArticle() != null && !maintenanceThreadViewModel.getMakeThisTopLevelArticle().isEmpty()
                    && maintenanceThreadViewModel.getEditArticleParentId() > 0) {


                boolean makeThreadTopLevelSuccess = true;

                //set top level from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.setThreadAsTopLevelArticle(app.getId(), maintenanceThreadViewModel.getEditArticleId())) {
                        log.error("Failed to mark thread as top level thread: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        makeThreadTopLevelSuccess = false;
                    }
                }

                //set message for user
                if (makeThreadTopLevelSuccess) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully made message top level article.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to make message top level article.");
                }
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //report abuse
            if (maintenanceThreadViewModel.getReportAbuse() != null && !maintenanceThreadViewModel.getReportAbuse().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                boolean threadsReportedSuccess = true;

                //report abuse from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.reportThreadForAbuse(app.getId(), maintenanceThreadViewModel.getEditArticleId(), user.getId())) {
                        log.error("Failed to report thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        threadsReportedSuccess = false;
                    }
                } else {

                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.reportThreadForAbuse(app.getId(), threadId, user.getId())) {
                                log.error("Failed to report thread id: " + threadId + " for appId: " + app.getId());
                                threadsReportedSuccess = false;
                            }
                        }
                    }
                }

                if (threadsReportedSuccess) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully reported and deleted messages.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to report and delete messages.");
                }
                //set view back to thread list view
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //approve message
            if (maintenanceThreadViewModel.getApprove() != null && !maintenanceThreadViewModel.getApprove().isEmpty()) {

                boolean threadApproved = true;

                //report abuse from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    Thread thread = threadService.getThreadById(maintenanceThreadViewModel.getEditArticleId());
                    if (thread != null && thread.getApplicationId().equals(app.getId())) {
                        thread.setApproved(true);
                        thread.setModDt(new Date());
                        if (threadService.saveThread(thread, thread.getBody()) == null) {
                            log.error("Failed to approve thread: " + thread.toString());
                            threadApproved = false;
                        } else {
                            log.info("Thread marked as approved by user: " + username + " :: " + thread.toString());
                        }
                    }
                } else {

                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            Thread thread = threadService.getThreadById(threadId);
                            if (thread != null && thread.getApplicationId().equals(app.getId())) {
                                thread.setApproved(true);
                                thread.setModDt(new Date());
                                if (threadService.saveThread(thread, thread.getBody()) == null) {
                                    log.error("Failed to approve thread: " + thread.toString());
                                    threadApproved = false;
                                } else {
                                    log.info("Thread marked as approved by user: " + username + " :: " + thread.toString());
                                }
                            } else {
                                log.warn("Thread id: " + threadIdStr + " for appId: " + app.getId()
                                        + " Either does not exist or does not belong to thread. Cannot be approved.");
                            }
                        }
                    }
                }

                if (threadApproved) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully approved messages.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to approve messages.");
                }
                //set view back to thread list view
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //search messages
            if (maintenanceThreadViewModel.getFindMessages() != null && !maintenanceThreadViewModel.getFindMessages().isEmpty()) {

                //set off of unapproved so default is approved if user does not have permission to search unapproved threads
                boolean isApproved = !"unapproved".equalsIgnoreCase(maintenanceThreadViewModel.getApprovedSearch());

                List<Thread> searchResults = threadService.searchThreadsByFields(
                        app.getId(),
                        maintenanceThreadViewModel.getAuthorSearch(),
                        maintenanceThreadViewModel.getEmailSearch(),
                        maintenanceThreadViewModel.getSubjectSearch(),
                        maintenanceThreadViewModel.getIpSearch(),
                        maintenanceThreadViewModel.getMessageSearch(),
                        isApproved
                );

                String searchResultsHtml = getListThreadHtml(searchResults, SEARCH_TAB);
                List<String> threadHtml = new ArrayList<>();
                threadHtml.add(searchResultsHtml);
                maintenanceThreadViewModel.setEditThreadTreeHtml(threadHtml);
                maintenanceThreadViewModel.setNumberOfMessages(searchResults.size());
                maintenanceThreadViewModel.setSearchSubmitted(true);

            }

            //search again
            if (maintenanceThreadViewModel.getSearchAgain() != null && !maintenanceThreadViewModel.getSearchAgain().isEmpty()) {
                maintenanceThreadViewModel.setSearchSubmitted(false);
                maintenanceThreadViewModel.setTab(SEARCH_TAB);
            }

            //post new message
            if (maintenanceThreadViewModel.getPostArticle() != null && !maintenanceThreadViewModel.getPostArticle().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                if (user != null && user.getIsUserAccount()) {

                    if (maintenanceThreadViewModel.getNewThreadSubject() != null
                            && !maintenanceThreadViewModel.getNewThreadSubject().trim().isEmpty()) {

                        String subject = inputHelper.sanitizeInput(maintenanceThreadViewModel.getNewThreadSubject());


                        //set ip address and user agent
                        String ipAddress = null;
                        String userAgent = null;
                        if (request != null) {
                            //check forwarded header for proxy users, if not found, use ip provided.
                            ipAddress = request.getHeader("X-FORWARDED-FOR");
                            if (ipAddress == null || ipAddress.isEmpty()) {
                                ipAddress = request.getRemoteAddr();
                            }
                            userAgent = request.getHeader(HttpHeaders.USER_AGENT);
                        }

                        Thread newThread = new Thread();
                        newThread.setSubmitter(user.getUsername());
                        newThread.setDiscAppUser(user);
                        newThread.setParentId(0L);
                        newThread.setShowEmail(user.getShowEmail());
                        newThread.setEmail(user.getEmail());
                        newThread.setDeleted(false);
                        newThread.setApplicationId(app.getId());
                        newThread.setSubject(subject);
                        newThread.setModDt(new Date());
                        newThread.setCreateDt(new Date());
                        //newThread.setApproved(isApproved);
                        newThread.setApproved(true);
                        newThread.setIpAddress(ipAddress);
                        newThread.setUserAgent(userAgent);

                        String body = maintenanceThreadViewModel.getNewThreadMessage();
                        if (body != null && !body.isEmpty()) {
                            body = body.replaceAll("\r", "<br />");

                            body = inputHelper.addUrlHtmlLinksToString(body);
                        }

                        threadService.saveThread(newThread, body);
                    } else {
                        maintenanceThreadViewModel.setInfoMessage("A subject is required to post a new message.");
                    }
                } else {
                    //TODO : maintenance accounts should post as logged out users. Page should ask for name/email
                    //TODO : etc just like regular disc app does.
                    maintenanceThreadViewModel.setInfoMessage("Maintenance system accounts cannot post new messages here. Please log in with a registered user account.");
                }
                //return to thread tab.
                maintenanceThreadViewModel.setTab(THREAD_TAB);
                currentTab = THREAD_TAB;
            }

            //selected to modify a single message
            if (maintenanceThreadViewModel.getEditArticle() != null && !maintenanceThreadViewModel.getEditArticle().isEmpty() && maintenanceThreadViewModel.isOnEditMessage()) {

                Thread threadToEdit = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());

                if (threadToEdit != null && threadToEdit.getApplicationId().equals(app.getId())) {
                    maintenanceThreadViewModel.setEditArticleSubmitter(threadToEdit.getSubmitter());
                    maintenanceThreadViewModel.setEditArticleEmail(threadToEdit.getEmail());
                    maintenanceThreadViewModel.setEditArticleSubject(threadToEdit.getSubject());
                    maintenanceThreadViewModel.setEditArticleMessage(threadToEdit.getBody());
                    maintenanceThreadViewModel.setApplicationId(app.getId());

                    String threadBody = threadService.getThreadBodyText(threadToEdit.getId());
                    maintenanceThreadViewModel.setEditArticleMessage(threadBody);

                    ThreadTreeNode subThreads = threadService.getFullThreadTree(threadToEdit.getId());
                    if (subThreads != null) {
                        String subThreadHtml = getEditThreadHtml(subThreads, "", true, false,
                                maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab());
                        maintenanceThreadViewModel.setEditArticleReplyThreadsHtml(subThreadHtml);
                    }

                    maintenanceThreadViewModel.setOnEditModifyMessage(true);

                } else {
                    log.warn("Failed to find thread to edit: " + maintenanceThreadViewModel.getEditArticleId() + " : appId: " + app.getId());
                    maintenanceThreadViewModel.setOnEditModifyMessage(false);
                    maintenanceThreadViewModel.setOnEditMessage(false);
                    maintenanceThreadViewModel.setInfoMessage("An error occurred loading message to edit. Please try again.");
                }
            }

            //submitted modified message
            if (maintenanceThreadViewModel.getEditArticleChangeMessage() != null && !maintenanceThreadViewModel.getEditArticleChangeMessage().isEmpty()
                    && maintenanceThreadViewModel.isOnEditModifyMessage()) {

                Thread editThread = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());
                if (editThread != null && editThread.getApplicationId().equals(app.getId())) {
                    String submitter = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleSubmitter());
                    String email = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleEmail());
                    String subject = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleSubject());

                    editThread.setSubmitter(submitter);
                    editThread.setEmail(email);
                    editThread.setSubject(subject);
                    editThread.setModDt(new Date());

                    String body = maintenanceThreadViewModel.getEditArticleMessage();

                    if (threadService.saveThread(editThread, body) != null) {
                        maintenanceThreadViewModel.setInfoMessage("Successfully edited thread.");
                    } else {
                        log.error("Failed to edit thread: " + maintenanceThreadViewModel.getEditArticleId() + " : appId: " + app.getId());
                        maintenanceThreadViewModel.setInfoMessage("Failed to update thread.");
                    }

                    maintenanceThreadViewModel.setOnEditModifyMessage(false);
                    maintenanceThreadViewModel.setOnEditMessage(false);
                }
            }

            //cancel button clicked on edit modify message screen
            if (maintenanceThreadViewModel.getEditArticleCancelEdit() != null && !maintenanceThreadViewModel.getEditArticleCancelEdit().isEmpty()) {
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);
            }

        } catch (Exception ex) {
            log.error("Thread administration action failed: " + ex.getMessage(), ex);
            maintenanceThreadViewModel.setInfoMessage("Error has occurred. Please try again.");
        }

        if (!maintenanceThreadViewModel.isOnEditMessage()) {
            maintenanceThreadViewModel.setOnEditMessage(false);
        }

        return getDiscEditView(appId, currentTab, maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel, model, response);
    }

    @GetMapping(THREADS_EDIT_PAGE)
    public ModelAndView getDiscEditView(@RequestParam(name = "id") long appId,
                                        @RequestParam(name = "tab", required = false) String currentTab,
                                        @RequestParam(name = "page", required = false) Integer page,
                                        @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                        Model model,
                                        HttpServletResponse response) {

        if (currentTab == null || currentTab.isEmpty()) {
            currentTab = THREAD_TAB;
        }

        maintenanceThreadViewModel.setTab(currentTab);

        if (page != null && page >= 0) {
            maintenanceThreadViewModel.setCurrentPage(page);
        }

        //set page to 0 if it's not already set
        if (maintenanceThreadViewModel.getCurrentPage() == null || maintenanceThreadViewModel.getCurrentPage() < 0) {
            maintenanceThreadViewModel.setCurrentPage(0);
        }

        //update page to next or previous if those buttons were selected.
        if (maintenanceThreadViewModel.getNextPageSubmit() != null) {
            maintenanceThreadViewModel.setCurrentPage(maintenanceThreadViewModel.getNextPage());
        }
        if (maintenanceThreadViewModel.getPreviousPageSubmit() != null) {
            maintenanceThreadViewModel.setCurrentPage(maintenanceThreadViewModel.getPreviousPage());
        }

        //if current page isn't 0, there's a previous page.
        if (maintenanceThreadViewModel.getCurrentPage() > 0) {
            maintenanceThreadViewModel.setHasPreviousPage(true);
        }

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceThreadViewModel.setApplicationId(app.getId());

            DiscAppUser user = discAppUserDetailsService.getByEmail(username);
            model.addAttribute(POSTING_USERNAME, user.getUsername());

            //enable the unapproved messages tab if they exist.
            List<Thread> unapprovedThreads = threadService.getUnapprovedThreads(app.getId());
            if (unapprovedThreads != null && unapprovedThreads.size() > 0) {
                maintenanceThreadViewModel.setHasUnapprovedMessages(true);
            }

            if (!maintenanceThreadViewModel.getTab().equals(SEARCH_TAB) && !maintenanceThreadViewModel.getTab().equals(UNAPPROVED_TAB)) {
                //get edit threads html
                List<String> threadTreeHtml = new ArrayList<>();

                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), maintenanceThreadViewModel.getCurrentPage(), 20, ThreadSortOrder.CREATION, true);

                //if threads returned is less than asked for, there is no next page.
                if (threadTreeNodeList.size() < 20) {
                    maintenanceThreadViewModel.setHasNextPage(false);
                } else {
                    maintenanceThreadViewModel.setHasNextPage(true);
                }

                if (maintenanceThreadViewModel.getTab().equals(THREAD_TAB)) {
                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                        String currentHtml = getEditThreadHtml(threadTreeNode, "<ul>", false, true,
                                maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab());
                        currentHtml += "</ul>";
                        threadTreeHtml.add(currentHtml);
                    }
                } else if (maintenanceThreadViewModel.getTab().equals(DATE_TAB)) {
                    String currentHtml = getEditThreadListHtml(threadTreeNodeList, maintenanceThreadViewModel.getCurrentPage(),
                            maintenanceThreadViewModel.getTab());
                    threadTreeHtml.add(currentHtml);
                }

                //todo : something wonky going on here. this is set here but the others are set in the above method..?
                //todo: setting above does not work.
                if (maintenanceThreadViewModel.isOnEditMessage()) {
                    Thread threadToEdit = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());

                    if (threadToEdit != null && threadToEdit.getApplicationId().equals(app.getId())) {
                        ThreadTreeNode subThreads = threadService.getFullThreadTree(threadToEdit.getId());
                        if (subThreads != null) {
                            String subThreadHtml = getEditThreadHtml(subThreads, "", true, false,
                                    maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab());
                            maintenanceThreadViewModel.setEditArticleReplyThreadsHtml(subThreadHtml);
                        }
                    }
                }

                maintenanceThreadViewModel.setEditThreadTreeHtml(threadTreeHtml);
                maintenanceThreadViewModel.setNumberOfMessages(threadService.getTotalThreadCountForApplicationId(app.getId()));

            } else if (maintenanceThreadViewModel.getTab().equalsIgnoreCase(UNAPPROVED_TAB)) {
                //unapproved thread tab is selected.
                if (unapprovedThreads != null) {
                    String unapprovedThreadsResultHtml = getListThreadHtml(unapprovedThreads, UNAPPROVED_TAB);
                    List<String> threadHtml = new ArrayList<>();
                    threadHtml.add(unapprovedThreadsResultHtml);
                    maintenanceThreadViewModel.setEditThreadTreeHtml(threadHtml);
                    maintenanceThreadViewModel.setNumberOfMessages(unapprovedThreads.size());
                }
            }
        } catch (Exception ex) {
            log.error("Error: " + ex.getMessage(), ex);
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }
        return new ModelAndView("admin/disc-edit", "maintenanceThreadViewModel", maintenanceThreadViewModel);
    }

    @GetMapping(THREAD_EDIT_PAGE)
    public ModelAndView getEditThreadView(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "article") long threadId,
                                          @RequestParam(name = "page", required = false) Integer page,
                                          @RequestParam(name = "tab", required = false) String tab,
                                          @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                          Model model,
                                          HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceThreadViewModel.setCurrentPage(page);

            if (tab != null && !tab.isEmpty()) {
                maintenanceThreadViewModel.setTab(tab);
            }

            maintenanceThreadViewModel.setOnEditMessage(true);

            Thread editingThread = threadService.getThread(app.getId(), threadId);
            if (editingThread != null) {
                maintenanceThreadViewModel.setEditArticleId(editingThread.getId());
                maintenanceThreadViewModel.setEditArticleParentId(editingThread.getParentId());
                maintenanceThreadViewModel.setEditArticleSubmitter(editingThread.getSubmitter());
                maintenanceThreadViewModel.setEditArticleEmail(editingThread.getEmail());
                maintenanceThreadViewModel.setEditArticleSubject(editingThread.getSubject());
                maintenanceThreadViewModel.setEditArticleCreateDt(editingThread.getCreateDt().toString());
                maintenanceThreadViewModel.setEditArticleModDt(editingThread.getModDt().toString());
                maintenanceThreadViewModel.setEditArticleIpAddress(editingThread.getIpAddress());
                maintenanceThreadViewModel.setEditArticleMessage(editingThread.getBody());
                maintenanceThreadViewModel.setEditArticleUserAgent(editingThread.getUserAgent());

                if (editingThread.getDiscAppUser() != null) {
                    maintenanceThreadViewModel.setEditArticleUserEmail(editingThread.getDiscAppUser().getEmail());
                    maintenanceThreadViewModel.setEditArticleCurrentUsername(editingThread.getDiscAppUser().getUsername());
                    maintenanceThreadViewModel.setEditArticleUserId(editingThread.getDiscAppUser().getId());
                }
            }
        } catch (Exception ex) {
            log.error("Error getting edit thread view: " + ex.getMessage(), ex);
        }

        return getDiscEditView(appId, maintenanceThreadViewModel.getTab(), maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel, model, response);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "appearance-preview.cgi")
    public ModelAndView getAppearancePreviewView(@RequestParam(name = "id") long appId,
                                                 Model model,
                                                 HttpServletRequest request) {
        return discAppController.getAppView(appId, 0, model, request);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "appearance-frameset.cgi")
    public ModelAndView getAppearanceView(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //app config
            maintenanceViewModel.setApplicationCreateDt(app.getCreateDt());
            maintenanceViewModel.setApplicationModDt(app.getModDt());
            maintenanceViewModel.setApplicationId(app.getId());
            maintenanceViewModel.setApplicationName(app.getName());

            //prologue / epilogue config
            Prologue prologue = applicationService.getPrologue(appId, false);
            if (prologue != null) {
                maintenanceViewModel.setPrologueModDt(prologue.getModDt());
                maintenanceViewModel.setPrologueText(prologue.getText());
            }

            Epilogue epilogue = applicationService.getEpilogue(appId, false);
            if (epilogue != null) {
                maintenanceViewModel.setEpilogueModDt(epilogue.getModDt());
                maintenanceViewModel.setEpilogueText(epilogue.getText());
            }

            //stylesheet config
            String styleSheetUrl = configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "");
            maintenanceViewModel.setStyleSheetUrl(styleSheetUrl);

            //threads config
            String sortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.CREATION.name());
            maintenanceViewModel.setThreadSortOrder(sortOrder);

            boolean expandThreadsOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, false);
            maintenanceViewModel.setExpandThreadsOnIndex(expandThreadsOnIndex);

            boolean previewFirstMessageOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, false);
            maintenanceViewModel.setPreviewFirstMessageOnIndex(previewFirstMessageOnIndex);

            boolean highlightNewMessages = configurationService.getBooleanValue(appId, ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, false);
            maintenanceViewModel.setHighlightNewMessages(highlightNewMessages);

            String threadBreak = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />");
            maintenanceViewModel.setThreadBreak(threadBreak);

            String entryBreak = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
            maintenanceViewModel.setEntryBreak(entryBreak);

            int maxThreadCountPerPage = configurationService.getIntegerValue(appId, ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, 20);
            maintenanceViewModel.setMaxThreadCountPerPage(maxThreadCountPerPage);

            int threadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 15);
            maintenanceViewModel.setThreadDepth(threadDepth);

            int previewLengthTopLevel = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, 320);
            maintenanceViewModel.setPreviewTopLevelLength(previewLengthTopLevel);

            int previewLengthReply = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, 200);
            maintenanceViewModel.setPreviewReplyLength(previewLengthReply);

            //header footer configs
            String headerText = configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, "");
            maintenanceViewModel.setHeader(headerText);

            String footerText = configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, "");
            maintenanceViewModel.setFooter(footerText);

            //label configs
            String authorHeader = configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Author");
            maintenanceViewModel.setAuthorHeader(authorHeader);

            String dateHeader = configurationService.getStringValue(appId, ConfigurationProperty.DATE_LABEL_TEXT, "Date");
            maintenanceViewModel.setDateHeader(dateHeader);

            String emailHeader = configurationService.getStringValue(appId, ConfigurationProperty.EMAIL_LABEL_TEXT, "Email");
            maintenanceViewModel.setEmailHeader(emailHeader);

            String subjectHeader = configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject");
            maintenanceViewModel.setSubjectHeader(subjectHeader);

            String messageHeader = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message");
            maintenanceViewModel.setMessageHeader(messageHeader);

            //buttons config
            String shareButton = configurationService.getStringValue(appId, ConfigurationProperty.SHARE_BUTTON_TEXT, "Share");
            maintenanceViewModel.setShareButton(shareButton);

            String editButton = configurationService.getStringValue(appId, ConfigurationProperty.EDIT_BUTTON_TEXT, "Edit");
            maintenanceViewModel.setEditButton(editButton);

            String returnButton = configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages");
            maintenanceViewModel.setReturnButton(returnButton);

            String previewButton = configurationService.getStringValue(appId, ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview");
            maintenanceViewModel.setPreviewButton(previewButton);

            String postButton = configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message");
            maintenanceViewModel.setPostButton(postButton);

            String previousPageButton = configurationService.getStringValue(appId, ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, "Previous Page");
            maintenanceViewModel.setPreviousPageButton(previousPageButton);

            String nextPageButton = configurationService.getStringValue(appId, ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page");
            maintenanceViewModel.setNextPageButton(nextPageButton);

            String replyButton = configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
            maintenanceViewModel.setReplyButton(replyButton);

            //favicon config
            String favicon = configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico");
            maintenanceViewModel.setFavicon(favicon);

        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return new ModelAndView("admin/appearance-forms", "maintenanceViewModel", maintenanceViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/application")
    public ModelAndView getModifyApplication(@RequestParam(name = "id") long appId) {

        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/prologue-epilogue")
    public ModelAndView getModifyPrologueEpilogue(@RequestParam(name = "id") long appId) {

        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/stylesheet")
    public ModelAndView getModifyStyleSheet(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/threads")
    public ModelAndView getModifyThreads(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/header-footer")
    public ModelAndView getModifyHeaderFooter(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/labels")
    public ModelAndView getModifyLabels(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/buttons")
    public ModelAndView getModifyButtons(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/favicon")
    public ModelAndView getModifyFavicon(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/time")
    public ModelAndView getModifyTime(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/application")
    public ModelAndView postModifyApplication(@RequestParam(name = "id") long appId,
                                              @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                              Model model,
                                              HttpServletResponse response) {

        if (maintenanceViewModel.getApplicationName() == null || maintenanceViewModel.getApplicationName().isEmpty()) {
            log.warn("Cannot update application name to an empty string");
            maintenanceViewModel.setInfoMessage("Cannot update disc app name to an empty value.");
            return getAppearanceView(appId, maintenanceViewModel, model, response);
        }

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {
                Application app = applicationService.get(appId);

                log.info("Updating application name of id: " + app.getId() + " from: "
                        + maintenanceViewModel.getApplicationName() + " to: " + app.getName());

                String updatedAppName = inputHelper.sanitizeInput(maintenanceViewModel.getApplicationName());

                app.setName(updatedAppName);
                app.setModDt(new Date());

                applicationService.save(app);

                maintenanceViewModel.setInfoMessage("Successfully updated application name.");

            } else {
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/stylesheet")
    public ModelAndView postModifyStyleSheet(@RequestParam(name = "id") long appId,
                                             @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                             Model model,
                                             HttpServletResponse response) {

        if (maintenanceViewModel.getStyleSheetUrl() == null || maintenanceViewModel.getStyleSheetUrl().isEmpty()) {
            maintenanceViewModel.setInfoMessage("Style sheet URL cannot be empty. Settings not saved.");
            return getAppearanceView(appId, maintenanceViewModel, model, response);
        }

        Application app = applicationService.get(appId);

        String styleSheetUrl = inputHelper.sanitizeInput(maintenanceViewModel.getStyleSheetUrl());

        if (!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, styleSheetUrl)) {
            maintenanceViewModel.setInfoMessage("Failed to update Style Sheet URL.");
        } else {
            maintenanceViewModel.setInfoMessage("Successfully updated Style Sheet URL.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/prologue-epilogue")
    public ModelAndView postModifyPrologueEpilogue(@RequestParam(name = "id") long appId,
                                                   @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                                   Model model,
                                                   HttpServletResponse response) {

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {
                Application app = applicationService.get(appId);

                //update prologue if text has changed.
                Prologue prologue = applicationService.getPrologue(app.getId(), false);

                if (prologue == null) {
                    prologue = new Prologue();
                    prologue.setApplicationId(app.getId());
                    prologue.setCreateDt(new Date());
                    prologue.setText("");
                }

                if (!maintenanceViewModel.getPrologueText().equals(prologue.getText())) {
                    log.info("Updating prologue text of id: " + app.getId());
                    prologue.setModDt(new Date());
                    prologue.setText(maintenanceViewModel.getPrologueText());

                    Prologue saved = applicationService.savePrologue(prologue);
                    if (saved != null) {
                        maintenanceViewModel.setInfoMessage("Prologue updated successfully. ");
                    } else {
                        maintenanceViewModel.setInfoMessage("Failed to save prologue. ");
                    }
                }

                Epilogue epilogue = applicationService.getEpilogue(app.getId(), false);

                if (epilogue == null) {
                    epilogue = new Epilogue();
                    epilogue.setApplicationId(app.getId());
                    epilogue.setCreateDt(new Date());
                    epilogue.setText("");
                }

                if (!maintenanceViewModel.getEpilogueText().equals(epilogue.getText())) {
                    log.info("Updating epilogue text of id: " + app.getId());
                    epilogue.setModDt(new Date());
                    epilogue.setText(maintenanceViewModel.getEpilogueText());

                    Epilogue savedEpilogue = applicationService.saveEpilogue(epilogue);
                    String currentMessage = maintenanceViewModel.getInfoMessage();
                    if (currentMessage == null) {
                        currentMessage = "";
                    }

                    if (savedEpilogue != null) {
                        currentMessage += " Epilogue updated successfully.";
                    } else {
                        currentMessage += " Failed to save epilogue.";
                    }

                    maintenanceViewModel.setInfoMessage(currentMessage);
                }
            } else {
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/threads")
    public ModelAndView postModifyThreads(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        Application app = applicationService.get(appId);

        if (maintenanceViewModel.getThreadSortOrder() == null || maintenanceViewModel.getThreadSortOrder().isEmpty()) {
            maintenanceViewModel.setThreadSortOrder(ThreadSortOrder.CREATION.name());
        }

        boolean sortOrderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_SORT_ORDER, maintenanceViewModel.getThreadSortOrder());
        boolean expandSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isExpandThreadsOnIndex()));
        boolean previewSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isPreviewFirstMessageOnIndex()));
        boolean highlightSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, String.valueOf(maintenanceViewModel.isHighlightNewMessages()));
        boolean threadBreakSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_BREAK_TEXT, maintenanceViewModel.getThreadBreak());
        boolean entryBreakSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.ENTRY_BREAK_TEXT, maintenanceViewModel.getEntryBreak());
        boolean maxThreadCountSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.getMaxThreadCountPerPage()));
        boolean threadDepthSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.getThreadDepth()));
        boolean previewTopLevelSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, String.valueOf(maintenanceViewModel.getPreviewTopLevelLength()));
        boolean previewReplySaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, String.valueOf(maintenanceViewModel.getPreviewReplyLength()));

        if (sortOrderSaved && expandSaved & previewSaved && highlightSaved && threadBreakSaved && entryBreakSaved
                && maxThreadCountSaved && threadDepthSaved && previewTopLevelSaved && previewReplySaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to threads.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to threads.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/header-footer")
    public ModelAndView postModifyHeaderFooter(@RequestParam(name = "id") long appId,
                                               @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                               Model model,
                                               HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean headerSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.HEADER_TEXT, maintenanceViewModel.getHeader());
        boolean footerSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.FOOTER_TEXT, String.valueOf(maintenanceViewModel.getFooter()));

        if (headerSaved && footerSaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to header and footer.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to header and footer.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/labels")
    public ModelAndView postModifyLabels(@RequestParam(name = "id") long appId,
                                         @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                         Model model,
                                         HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean authorHeaderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SUBMITTER_LABEL_TEXT, maintenanceViewModel.getAuthorHeader());
        boolean dateHeaderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.DATE_LABEL_TEXT, String.valueOf(maintenanceViewModel.getDateHeader()));
        boolean emailSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EMAIL_LABEL_TEXT, String.valueOf(maintenanceViewModel.getEmailHeader()));
        boolean subjectSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SUBJECT_LABEL_TEXT, String.valueOf(maintenanceViewModel.getSubjectHeader()));
        boolean messageSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_BODY_LABEL_TEXT, String.valueOf(maintenanceViewModel.getMessageHeader()));

        if (authorHeaderSaved && dateHeaderSaved && emailSaved && subjectSaved && messageSaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to labels.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to labels.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/buttons")
    public ModelAndView postModifyButtons(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean shareButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SHARE_BUTTON_TEXT, maintenanceViewModel.getShareButton());
        boolean editButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EDIT_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getEditButton()));
        boolean returnButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReturnButton()));
        boolean previewButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPreviewButton()));
        boolean postButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPostButton()));
        boolean previousPageButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPreviousPageButton()));
        boolean nextPageButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getNextPageButton()));
        boolean replyButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReplyButton()));

        if (shareButtonSaved && editButtonSaved && returnButtonSaved && previewButtonSaved && postButtonSaved
                && previousPageButtonSaved && nextPageButtonSaved && replyButtonSaved) {

            maintenanceViewModel.setInfoMessage("Successfully saved changes to buttons.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to buttons.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/favicon")
    public ModelAndView postModifyFavicon(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        //default used if field is blank
        String favicon = "/favicon.ico";

        //get form value if not null.
        if (maintenanceViewModel.getFavicon() != null && !maintenanceViewModel.getFavicon().trim().isEmpty()) {
            favicon = inputHelper.sanitizeInput(maintenanceViewModel.getFavicon());
        }

        //if entered something like "www.somesite.com/favicon.ico", attempt to add http in front
        if (!favicon.startsWith("http://") &&
                !favicon.startsWith("https://") &&
                !favicon.startsWith("/")) {

            //if just want default favicon, add front slash, otherwise attempt to add http
            if (favicon.equalsIgnoreCase("favicon.ico")) {
                favicon = "/favicon.ico";
            } else {
                favicon = "http://" + favicon;
            }
        }

        Application app = applicationService.get(appId);

        if (!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.FAVICON_URL, favicon)) {
            maintenanceViewModel.setInfoMessage("Failed to update Favicon.");
        } else {
            maintenanceViewModel.setFavicon(favicon);
            maintenanceViewModel.setInfoMessage("Successfully updated Favicon.");
        }

        return getAppearanceView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/time")
    public ModelAndView postModifyTime(@RequestParam(name = "id") long appId,
                                       @ModelAttribute MaintenanceLocaleViewModel maintenanceLocaleViewModel,
                                       Model model,
                                       HttpServletResponse response) {

        if (maintenanceLocaleViewModel.getDateFormat() == null || maintenanceLocaleViewModel.getDateFormat().trim().isEmpty()) {
            maintenanceLocaleViewModel.setDateFormat("EEE MMM dd, yyyy h:mma");
        }

        Application app = applicationService.get(appId);

        String dateFormat = inputHelper.sanitizeInput(maintenanceLocaleViewModel.getDateFormat());

        boolean timezoneSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.TIMEZONE_LOCATION, maintenanceLocaleViewModel.getSelectedTimezone());
        boolean dateFormatSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.DATE_FORMAT_PATTERN, dateFormat);

        if (timezoneSaved && dateFormatSaved) {
            log.info("Saved date and time settings for appId: " + appId);
            maintenanceLocaleViewModel.setInfoMessage("Successfully saved changes to Date and Time.");
        } else {
            maintenanceLocaleViewModel.setInfoMessage("Failed to save changes to Date and Time.");
        }

        return getDiscLocaleView(appId, maintenanceLocaleViewModel, model, response);
    }


    /**
     * Saves updated or new configuration for an application
     *
     * @param appId    application id
     * @param property configuration property to save
     * @param value    values to save for the configuration
     * @return returns true on success and false on failure.
     */
    private boolean saveUpdatedConfiguration(long appId, ConfigurationProperty property, String value) {

        Configuration configToUpdate = configurationService.getConfiguration(appId, property.getPropName());

        if (configToUpdate == null) {
            log.info("Creating new configuration prop: " + property.getPropName() + " for appId: " + appId);
            configToUpdate = new Configuration();
            configToUpdate.setName(property.getPropName());
            configToUpdate.setApplicationId(appId);
        }

        configToUpdate.setValue(value);

        if (!configurationService.saveConfiguration(property, configToUpdate)) {
            log.warn("Failed to update configuration " + property.getPropName() + " of appId: " + appId);
            return false;
        } else {
            log.info("Updated " + property.getPropName() + " for appId: " + appId + " to " + value);
        }

        return true;
    }

    /**
     * Generates edit thread HTML for threads view which is a stripped down version of the default view.
     *
     * @param currentNode Node to create list HTML for
     * @param currentHtml Current html to be built upon
     * @return Returns generated HTML
     */
    private String getEditThreadHtml(ThreadTreeNode currentNode, String currentHtml, boolean skipCurrent,
                                     boolean includeCheckBox, int currentPage, String tab) {

        if (!skipCurrent) {
            currentHtml +=
                    "<li>" +
                            "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() + "&amp;page=" + currentPage
                            + "&amp;tab=" + tab + "\">" +
                            currentNode.getCurrent().getSubject() +
                            "</a> " +

                            "<label for=\"checkbox_" + currentNode.getCurrent().getId() + "\">" +
                            "    <span style=\"font-size:smaller;\">" +
                            "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                            currentNode.getCurrent().getSubmitter() + " " +
                            currentNode.getCurrent().getCreateDt() +
                            "        </span>" +
                            "    </span>" +
                            "</label>";
            if (includeCheckBox) {
                currentHtml += "<label>" +
                        "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + currentNode.getCurrent().getId() +
                        "\" id=\"checkbox_" + currentNode.getCurrent().getId() + "\"/>" +
                        "</label>";
            }

            currentHtml += "</li>";

        }
        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getEditThreadHtml(node, currentHtml, false, includeCheckBox, currentPage, tab);
            currentHtml += "</ul>";
        }

        return currentHtml;
    }

    /**
     * Gets HTML string for the edit thread view by date. Threads are sorted by date desc
     *
     * @param threadTreeNodeList ThreadTreeNode list to use to populate HTML string
     * @return Generated HTML for node list
     */
    private String getEditThreadListHtml(List<ThreadTreeNode> threadTreeNodeList, int currentPage, String tab) {

        StringBuilder currentHtml = new StringBuilder("<ul>");

        List<ThreadTreeNode> allThreads = new ArrayList<>();
        for (ThreadTreeNode node : threadTreeNodeList) {
            populateFlatThreadList(node, allThreads);
        }

        //sort threads by date asc.
        allThreads.sort((node1, node2) -> {
            if (node1.getCurrent().getId().equals(node2.getCurrent().getId())) {
                return 0;
            }
            return node1.getCurrent().getId() > node2.getCurrent().getId() ? -1 : 1;
        });


        for (ThreadTreeNode currentNode : allThreads) {
            currentHtml.append(
                    "<li>" +
                            "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() + "&amp;page=" + currentPage
                            + "&amp;tab=" + tab + "\">" +
                            currentNode.getCurrent().getSubject() +
                            "</a> " +

                            "<label for=\"checkbox_" + currentNode.getCurrent().getId() + "\">" +
                            "    <span style=\"font-size:smaller;\">" +
                            "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                            currentNode.getCurrent().getSubmitter() + " " +
                            currentNode.getCurrent().getCreateDt() +
                            "        </span>" +
                            "    </span>" +
                            "</label>" +
                            "<label>" +
                            "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + currentNode.getCurrent().getId() +
                            "\" id=\"checkbox_" + currentNode.getCurrent().getId() + "\" \"/>" +
                            "</label>" +
                            "</li>"
            );
        }
        currentHtml.append("</ul>");
        return currentHtml.toString();
    }

    /**
     * Populates the threadTreeNodeList with all of the sub threads passed into the current parameter as
     * a flat list.
     *
     * @param current            current ThreadTreeNode to start populating from (will traverse sub nodes)
     * @param threadTreeNodeList ThreadTreeNode list to add parent and sub nodes too from current node.
     */
    private void populateFlatThreadList(ThreadTreeNode current, List<ThreadTreeNode> threadTreeNodeList) {
        threadTreeNodeList.add(current);
        for (ThreadTreeNode threadTreeNode : current.getSubThreads()) {
            populateFlatThreadList(threadTreeNode, threadTreeNodeList);
        }
    }


    private String getListThreadHtml(List<Thread> threads, String tab) {
        String currentHtml = "<ul>";

        for (Thread thread : threads) {

            currentHtml += "<li>" +
                    "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + thread.getApplicationId() +
                    "&amp;article=" + thread.getId() +
                    "&amp;tab=" + tab + "\">" +
                    thread.getSubject() +
                    "</a> " +

                    "<label for=\"checkbox_" + thread.getId() + "\">" +
                    "    <span style=\"font-size:smaller;\">" +
                    "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                    thread.getSubmitter() + " " +
                    thread.getCreateDt() +
                    "        </span>" +
                    "    </span>" +
                    "</label>" +
                    "<label>" +
                    "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + thread.getId() +
                    "\" id=\"checkbox_" + thread.getId() + "\" \"/>" +
                    "</label>" +
                    "</li>";
        }
        currentHtml += "</ul>";
        return currentHtml;
    }

    private String getWidgetHtml(Integer width, String widthUnit, Integer height, String heightUnit, long appId, String baseUrl) {
        return "<div style=\"width:" + width + widthUnit + "; height:" + height + heightUnit + "; margin:4%; padding:1ex; \n" +
                "    border:1px solid black; float:right;\">\n" +
                "\n" +
                " Here's your widget! Put your header here.\n" +
                "\n" +
                "<iframe src=\"" + baseUrl + WidgetController.CONTROLLER_URL_DIRECTORY + "disc-widget.cgi?disc=" + appId + "\" \n" +
                "        width=\"99%\" height=\"80%\" frameborder=\"no\" scrolling=\"no\">\n" +
                "</iframe>\n" +
                "\n" +
                " Put your footer here.\n" +
                "\n" +
                "</div>\n";
    }

    private ApplicationIpBlock buildApplicationIpBlock(long appId, String ipPrefix) {
        ApplicationIpBlock ipBlock = new ApplicationIpBlock();
        ipBlock.setApplicationId(appId);
        ipBlock.setCreateDt(new Date());
        ipBlock.setModDt(new Date());
        ipBlock.setIpAddressPrefix(ipPrefix);
        return ipBlock;
    }

    private void setCommonModelAttributes(Model model, Application app, String username) {

        model.addAttribute(APP_NAME, app.getName());
        model.addAttribute(APP_ID, app.getId());
        model.addAttribute(USERNAME, username);

        if (!username.equals(String.valueOf(app.getId()))) {
            model.addAttribute(IS_USER_ACCOUNT, true);
        }

        if (applicationService.isOwnerOfApp(app.getId(), username)) {
            model.addAttribute(IS_OWNER, true);
        }

    }
}
