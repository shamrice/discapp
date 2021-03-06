package io.github.shamrice.discapp.web.controller.siteadmin;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.repository.*;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.site.SiteService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.siteadmin.*;
import io.github.shamrice.discapp.web.util.InputHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.SiteAdminUrl.*;

@Slf4j
@Controller
public class SiteAdminController {

    //TODO : this should speak to services, not the repos directly!!!

    @Autowired
    private DiscAppUserRepository discAppUserRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ImportDataRepository importDataRepository;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ApplicationSubscriptionRepository applicationSubscriptionRepository;

    @Autowired
    private ApplicationIpBlockRepository applicationIpBlockRepository;

    @Autowired
    private SiteService siteService;

    @Autowired
    private InputHelper inputHelper;

    private static String appVersion;

    private String getAppVersion() {
        if (appVersion == null) {
            appVersion = io.github.shamrice.discapp.Application.class.getPackage().getImplementationVersion();
            log.info("Website version: " + appVersion);
        }
        return appVersion;
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY)
    public ModelAndView getSiteAdmin(Model model) {
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/admin", "model", model);
    }

    @GetMapping(IP_BLOCK_URL)
    public ModelAndView getSiteAdminIpBlock(SiteAdminIpBlockViewModel siteAdminIpBlockViewModel, Model model) {
        List<ApplicationIpBlock> ipBlockList = applicationIpBlockRepository.findByApplicationId(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID);
        siteAdminIpBlockViewModel.setIpBlockList(ipBlockList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/ipBlock", "siteAdminIpBlockViewModel", siteAdminIpBlockViewModel);
    }

    @GetMapping(IP_BLOCK_REMOVE)
    public ModelAndView getSiteAdminIpBlockRemove(@RequestParam long id,
                                                  SiteAdminIpBlockViewModel siteAdminIpBlockViewModel,
                                                  Model model) {
        try {
            ApplicationIpBlock blockToRemove = applicationIpBlockRepository.getOne(id);
            if (blockToRemove.getApplicationId().equals(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID)) {
                log.info("Found IP Block record to remove. Id: " + id);
                applicationIpBlockRepository.delete(blockToRemove);
                siteAdminIpBlockViewModel.setInfoMessage("Removed IP Block for ID: " + id);
            } else {
                siteAdminIpBlockViewModel.setErrorMessage("Unable to find record for id or not owned by main site: " + id);
            }
        } catch (Exception ex) {
            log.error("Failed to remove ip block id: " + id, ex);
            siteAdminIpBlockViewModel.setErrorMessage("Failed to remove id: " + id + ". " + ex.getMessage());
        }

        List<ApplicationIpBlock> ipBlockList = applicationIpBlockRepository.findByApplicationId(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID);
        siteAdminIpBlockViewModel.setIpBlockList(ipBlockList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/ipBlock", "siteAdminIpBlockViewModel", siteAdminIpBlockViewModel);
    }

    @PostMapping(IP_BLOCK_ADD)
    public ModelAndView postSiteAdminIpBlockAdd(SiteAdminIpBlockViewModel siteAdminIpBlockViewModel, Model model) {

        if (siteAdminIpBlockViewModel.getNewIpBlockPrefix() != null && !siteAdminIpBlockViewModel.getNewIpBlockPrefix().isEmpty()) {
            //create new record
            ApplicationIpBlock newApplicationIpBlock = new ApplicationIpBlock();
            newApplicationIpBlock.setApplicationId(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID);
            newApplicationIpBlock.setIpAddressPrefix(siteAdminIpBlockViewModel.getNewIpBlockPrefix());
            newApplicationIpBlock.setReason(siteAdminIpBlockViewModel.getNewIpBlockReason());
            newApplicationIpBlock.setCreateDt(new Date());
            newApplicationIpBlock.setModDt(new Date());
            //save it
            applicationIpBlockRepository.save(newApplicationIpBlock);

            log.info("Created new site wide application block for IP Prefix: " + newApplicationIpBlock.getIpAddressPrefix());
            siteAdminIpBlockViewModel.setInfoMessage("New block added for IP Prefix: " + newApplicationIpBlock.getIpAddressPrefix());
        } else {
            siteAdminIpBlockViewModel.setErrorMessage("Please enter an IP prefix to block.");
        }

        List<ApplicationIpBlock> ipBlockList = applicationIpBlockRepository.findByApplicationId(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID);
        siteAdminIpBlockViewModel.setIpBlockList(ipBlockList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/ipBlock", "siteAdminIpBlockViewModel", siteAdminIpBlockViewModel);
    }

    @GetMapping(SUBSCRIBERS_URL)
    public ModelAndView getSiteAdminSubscribers(SiteAdminSubscriberViewModel siteAdminSubscriberViewModel, Model model) {
        List<ApplicationSubscription> subscriptionList = applicationSubscriptionRepository.findAll(Sort.by("applicationId").ascending());
        siteAdminSubscriberViewModel.setApplicationSubscriptions(subscriptionList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/subscribers", "siteAdminSubscriberViewModel", siteAdminSubscriberViewModel);
    }

    @GetMapping(SUBSCRIBER_ENABLED)
    public ModelAndView getSiteAdminSubscriberEnabled(@RequestParam(name = "id") Long applicationSubscriptionId,
                                                      @RequestParam(name = "enabled") Boolean enabled,
                                                      SiteAdminSubscriberViewModel siteAdminSubscriberViewModel,
                                                      Model model) {
        try {
            log.info("Setting application subscription id: " + applicationSubscriptionId + " enabled to: " + enabled);
            ApplicationSubscription subscription = applicationSubscriptionRepository.findById(applicationSubscriptionId).orElse(null);

            if (subscription != null) {
                subscription.setEnabled(enabled);
                subscription.setModDt(new Date());
                applicationSubscriptionRepository.save(subscription);
                siteAdminSubscriberViewModel.setInfoMessage("Updated " + subscription.getSubscriberEmail() + " enabled to: " + enabled);
            }
        } catch (Exception ex) {
            log.error("Failed to set enabled settings for subscriber id: " + applicationSubscriptionId + " to: " + enabled);
            siteAdminSubscriberViewModel.setErrorMessage("Failed to set subscriber Id: " + applicationSubscriptionId + " enabled to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminSubscribers(siteAdminSubscriberViewModel, model);
    }

    @GetMapping(ACCOUNTS_URL)
    public ModelAndView getSiteAdminAccounts(@RequestParam(name = "type", required = false, defaultValue = "user") String userType,
                                             SiteAdminAccountViewModel siteAdminAccountViewModel,
                                             Model model) {
        List<DiscAppUser> fullUserList = discAppUserRepository.findAll(Sort.by("id").ascending());

        //filter accounts based on query param as well as remove deleted accounts
        //probably could be done in one pass instead of 2...
        fullUserList.removeIf(user -> user.getUsername() != null && user.getUsername().contains("_DELETED_") && !user.getEnabled());

        boolean onlyUserAccounts = "user".equalsIgnoreCase(userType);
        if (onlyUserAccounts) {
            fullUserList.removeIf(user -> user.getIsUserAccount() != null && !user.getIsUserAccount());
        } else {
            fullUserList.removeIf(user -> user.getIsUserAccount() != null && user.getIsUserAccount());
        }

        List<SiteAdminAccountViewModel.User> modelUserList = new ArrayList<>();

        //this is a stupid way to just format the date columns...
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        for (DiscAppUser user : fullUserList) {
            SiteAdminAccountViewModel.User u = new SiteAdminAccountViewModel.User();
            u.setId(user.getId());
            u.setUsername(user.getUsername());
            u.setEmail(user.getEmail());
            u.setShowEmail(user.getShowEmail());
            u.setOwnerId(user.getOwnerId());
            u.setEnabled(user.getEnabled());
            u.setIsAdmin(user.getIsAdmin());
            u.setIsUserAccount(user.getIsUserAccount());
            if (user.getLastLoginDate() != null) u.setLastLoginDate(dateFormat.format(user.getLastLoginDate()));
            if (user.getCreateDt() != null) u.setCreateDt(dateFormat.format(user.getCreateDt()));
            if (user.getModDt() != null) u.setModDt(dateFormat.format(user.getModDt()));

            modelUserList.add(u);
        }

        siteAdminAccountViewModel.setUserType(userType);
        siteAdminAccountViewModel.setUserList(modelUserList);

        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }


    @GetMapping(ACCOUNT_SHOW_EMAIL)
    public ModelAndView getSiteAdminAccountShowEmail(@RequestParam(name = "id") Long userId,
                                                     @RequestParam(name = "enabled") Boolean enabled,
                                                     @RequestParam(name = "type", required = false, defaultValue = "user") String userType,
                                                     SiteAdminAccountViewModel siteAdminAccountViewModel,
                                                     Model model) {
        try {
            log.info("Setting user id: " + userId + " show email to: " + enabled);
            DiscAppUser discAppUser = discAppUserRepository.getOne(userId);
            discAppUser.setShowEmail(enabled);
            discAppUser.setModDt(new Date());
            discAppUserRepository.save(discAppUser);
            siteAdminAccountViewModel.setInfoMessage("Updated user id: " + userId + " show email to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set show email settings for disc app user id: " + userId + " to: " + enabled);
            siteAdminAccountViewModel.setErrorMessage("Failed to set userId: " + userId + " email enabled to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminAccounts(userType, siteAdminAccountViewModel, model);
    }

    @GetMapping(ACCOUNT_ENABLED)
    public ModelAndView getSiteAdminAccountEnabled(@RequestParam(name = "id") Long userId,
                                                   @RequestParam(name = "enabled") Boolean enabled,
                                                   @RequestParam(name = "type", required = false, defaultValue = "user") String userType,
                                                   SiteAdminAccountViewModel siteAdminAccountViewModel,
                                                   Model model) {
        try {
            log.info("Setting user id: " + userId + " enabled to: " + enabled);
            DiscAppUser discAppUser = discAppUserRepository.getOne(userId);
            discAppUser.setEnabled(enabled);
            discAppUser.setModDt(new Date());
            discAppUserRepository.save(discAppUser);
            siteAdminAccountViewModel.setInfoMessage("Updated user id: " + userId + " enabled to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set enabled settings for disc app user id: " + userId + " to: " + enabled);
            siteAdminAccountViewModel.setErrorMessage("Failed to set userId: " + userId + " enabled to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminAccounts(userType, siteAdminAccountViewModel, model);
    }

    @GetMapping(ACCOUNT_IS_ADMIN)
    public ModelAndView getSiteAdminIsAdmin(@RequestParam(name = "id") Long userId,
                                            @RequestParam(name = "enabled") Boolean enabled,
                                            @RequestParam(name = "type", required = false, defaultValue = "user") String userType,
                                            SiteAdminAccountViewModel siteAdminAccountViewModel,
                                            Model model) {
        try {
            log.info("Setting user id: " + userId + " is admin to: " + enabled);
            DiscAppUser discAppUser = discAppUserRepository.getOne(userId);
            discAppUser.setIsAdmin(enabled);
            discAppUser.setModDt(new Date());
            discAppUserRepository.save(discAppUser);
            siteAdminAccountViewModel.setInfoMessage("Updated user id: " + userId + " is admin to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set is admin settings for disc app user id: " + userId + " to: " + enabled);
            siteAdminAccountViewModel.setErrorMessage("Failed to set userId: " + userId + " is admin to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminAccounts(userType, siteAdminAccountViewModel, model);
    }

    @GetMapping(ACCOUNT_IS_USER_ACCOUNT)
    public ModelAndView getSiteAdminIsUserAccount(@RequestParam(name = "id") Long userId,
                                                  @RequestParam(name = "enabled") Boolean enabled,
                                                  @RequestParam(name = "type", required = false, defaultValue = "user") String userType,
                                                  SiteAdminAccountViewModel siteAdminAccountViewModel,
                                                  Model model) {
        try {
            log.info("Setting user id: " + userId + " is user account to: " + enabled);
            DiscAppUser discAppUser = discAppUserRepository.getOne(userId);
            discAppUser.setIsUserAccount(enabled);
            discAppUser.setModDt(new Date());
            discAppUserRepository.save(discAppUser);
            siteAdminAccountViewModel.setInfoMessage("Updated user id: " + userId + " is user account to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set is user account settings for disc app user id: " + userId + " to: " + enabled);
            siteAdminAccountViewModel.setErrorMessage("Failed to set userId: " + userId + " is user account to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminAccounts(userType, siteAdminAccountViewModel, model);
    }

    @GetMapping(OWNER_URL)
    public ModelAndView getSiteAdminOwnerInfo(@RequestParam(name = "ownerId") Long ownerId,
                                              @RequestParam(name = "type", defaultValue = "user", required = false) String userType,
                                              SiteAdminOwnerViewModel siteAdminOwnerViewModel,
                                              Model model) {
        try {
            Owner owner = ownerRepository.getOne(ownerId);
            siteAdminOwnerViewModel.setOwner(owner);
        } catch (Exception ex) {
            log.error("Failed to get owner with id: " + ownerId + " :: " + ex.getMessage(), ex);
            siteAdminOwnerViewModel.setErrorMessage("Unable to find owner with id: " + ownerId);
        }
        siteAdminOwnerViewModel.setUserType(userType);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/owner", "siteAdminOwnerViewModel", siteAdminOwnerViewModel);

    }

    @GetMapping(APPLICATIONS_URL)
    public ModelAndView getSiteAdminApplications(SiteAdminApplicationViewModel siteAdminApplicationViewModel,
                                                 Model model) {
        List<Application> fullApplicationList = applicationRepository.findAll(Sort.by("id").ascending());
        siteAdminApplicationViewModel.setApplicationList(fullApplicationList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/applications", "siteAdminApplicationViewModel", siteAdminApplicationViewModel);
    }

    @GetMapping(APPLICATION_ENABLED)
    public ModelAndView getSiteAdminApplicationEnabled(@RequestParam(name = "id") Long appId,
                                                       @RequestParam(name = "enabled") Boolean enabled,
                                                       SiteAdminApplicationViewModel siteAdminApplicationViewModel,
                                                       Model model) {
        try {
            if (appId <= 1) {
                siteAdminApplicationViewModel.setErrorMessage("Cannot disable help forum or base web site.");
                return getSiteAdminApplications(siteAdminApplicationViewModel, model);
            }

            log.info("Setting application id: " + appId + " enabled to: " + enabled);
            Application app =  applicationRepository.getOne(appId);
            app.setEnabled(enabled);
            app.setModDt(new Date());
            applicationRepository.save(app);
            siteAdminApplicationViewModel.setInfoMessage("Updated application id: " + appId + " enabled to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set enabled settings for application id: " + appId + " to: " + enabled);
            siteAdminApplicationViewModel.setErrorMessage("Failed to set appId: " + appId + " enabled to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminApplications(siteAdminApplicationViewModel, model);
    }

    @GetMapping(APPLICATION_DELETED)
    public ModelAndView getSiteAdminApplicationDeleted(@RequestParam(name = "id") Long appId,
                                                       @RequestParam(name = "enabled") Boolean enabled,
                                                       SiteAdminApplicationViewModel siteAdminApplicationViewModel,
                                                       Model model) {

        //TODO : use service to delete / undelete threads related t application being marked as deleted.

        try {
            if (appId <= 1) {
                siteAdminApplicationViewModel.setErrorMessage("Cannot delete help forum or base web site.");
                return getSiteAdminApplications(siteAdminApplicationViewModel, model);
            }

            log.info("Setting application id: " + appId + " deleted to: " + enabled);
            Application app =  applicationRepository.getOne(appId);
            app.setDeleted(enabled);
            app.setModDt(new Date());
            applicationRepository.save(app);
            siteAdminApplicationViewModel.setInfoMessage("Updated application id: " + appId + " deleted to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set deleted settings for application id: " + appId + " to: " + enabled);
            siteAdminApplicationViewModel.setErrorMessage("Failed to set appId: " + appId + " deleted to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminApplications(siteAdminApplicationViewModel, model);
    }

    @GetMapping(APPLICATION_IS_SEARCHABLE)
    public ModelAndView getSiteAdminApplicationIsSearchable(@RequestParam(name = "id") Long appId,
                                                       @RequestParam(name = "enabled") Boolean enabled,
                                                       SiteAdminApplicationViewModel siteAdminApplicationViewModel,
                                                       Model model) {
        try {
            log.info("Setting application id: " + appId + " is searchable to: " + enabled);
            Application app =  applicationRepository.getOne(appId);
            app.setSearchable(enabled);
            app.setModDt(new Date());
            applicationRepository.save(app);
            siteAdminApplicationViewModel.setInfoMessage("Updated application id: " + appId + " searchable to: " + enabled);
        } catch (Exception ex) {
            log.error("Failed to set searchable settings for application id: " + appId + " to: " + enabled);
            siteAdminApplicationViewModel.setErrorMessage("Failed to set appId: " + appId + " searchable to: " + enabled);
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminApplications(siteAdminApplicationViewModel, model);
    }

    @GetMapping(IMPORTS_URL)
    public ModelAndView getSiteAdminImports(SiteAdminImportViewModel siteAdminImportViewModel,
                                            Model model) {
        List<ImportData> fullImportDataList = importDataRepository.findAll(Sort.by("id").ascending());
        siteAdminImportViewModel.setImportDataList(fullImportDataList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/imports", "siteAdminImportViewModel", siteAdminImportViewModel);
    }

    @GetMapping(IMPORT_DELETE)
    public ModelAndView getSiteAdminImportDelete(@RequestParam(name = "id") Long importId,
                                                 SiteAdminImportViewModel siteAdminImportViewModel,
                                                 Model model) {

        try {
            log.info("Deleting import id: " + importId);
            importDataRepository.deleteById(importId);
            siteAdminImportViewModel.setInfoMessage("Successfully deleted import id: " + importId);
        } catch (Exception ex) {
            log.error("Failed to delete import id: " + importId);
            siteAdminImportViewModel.setErrorMessage("Failed to delete import id: " + importId + " :: " + ex.getMessage());
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminImports(siteAdminImportViewModel, model);
    }

    @GetMapping(IMPORT_DOWNLOAD)
    @ResponseBody
    public ResponseEntity<Resource> getSiteAdminImportDownload(@RequestParam(name = "id") long importId) {

        try {
            ImportData importData = importDataRepository.findById(importId).orElse(null);

            if (importData != null) {
                Resource file = new ByteArrayResource(importData.getImportData());
                return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + importData.getImportName() + "\"")
                        .body(file);
            }

        } catch (Exception ex) {
            log.error("Error downloading import id: " + importId + " :: " + ex.getMessage(), ex);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(THREADS_URL)
    public ModelAndView getSiteAdminThreads(SiteAdminThreadViewModel siteAdminThreadViewModel,
                                            Model model) {
        List<Thread> fullThreadList = threadService.getAllDeletedThreads();
        for (Thread thread : fullThreadList) {
            if (thread.getSubmitter() != null && thread.getSubmitter().length() > 10) {
                thread.setSubmitter(thread.getSubmitter().substring(0, 10) + "...");
            }
        }
        siteAdminThreadViewModel.setThreadList(fullThreadList);
        model.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/threads", "siteAdminThreadViewModel", siteAdminThreadViewModel);
    }

    @GetMapping(THREAD_RESTORE)
    public ModelAndView getSiteAdminThreadRestore(@RequestParam(name = "id") Long threadId,
                                                  SiteAdminThreadViewModel siteAdminThreadViewModel,
                                                  Model model) {
        try {
            log.info("Restoring thread id: " + threadId);
            Thread thread = threadService.getThreadById(threadId);
            thread.setDeleted(false);
            thread.setModDt(new Date());
            if (threadService.saveThread(thread, thread.getBody(), false) > 0) {
                log.info("Thread id: " + threadId + " restored successfully.");
                siteAdminThreadViewModel.setInfoMessage("Thread id: " + threadId + " restored successfully.");
            } else {
                log.warn("Failed to restore thread id: " + threadId);
                siteAdminThreadViewModel.setErrorMessage("Failed to restore thread id: " + threadId);
            }
        } catch (Exception ex) {
            log.error("Failed to restore thread id: " + threadId + " :: " + ex.getMessage(), ex);
            siteAdminThreadViewModel.setErrorMessage("Failed to restore thread id: " + threadId + " :: " + ex.getMessage());
        }
        model.addAttribute("appVersion", getAppVersion());
        return getSiteAdminThreads(siteAdminThreadViewModel, model);
    }

    @GetMapping(UPDATE_URL)
    public ModelAndView getSiteAdminUpdateView(SiteAdminUpdateViewModel model, Model headerModel) {
        headerModel.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/update", "siteAdminUpdateViewModel", model);
    }

    @PostMapping(UPDATE_URL)
    public ModelAndView postSiteAdminUpdateView(@ModelAttribute SiteAdminUpdateViewModel model, Model headerModel) {

        if (model.getNewUpdateText() != null && !model.getNewUpdateText().trim().isEmpty()
                && model.getNewUpdateSubject() != null && !model.getNewUpdateSubject().trim().isEmpty()) {

            if (model.getPreviewButton() != null && !model.getPreviewButton().isEmpty()) {
                model.setShowPreview(true);
                model.setUpdatePreviewText(addHtmlToUpdateBody(model.getNewUpdateText()));
                headerModel.addAttribute("appVersion", getAppVersion());
                return new ModelAndView("site_admin/update", "siteAdminUpdateViewModel", model);
            } else {
                String body = addHtmlToUpdateBody(model.getNewUpdateText());

                SiteUpdateLog siteUpdateLog = new SiteUpdateLog();
                siteUpdateLog.setSubject(model.getNewUpdateSubject());
                siteUpdateLog.setMessage(body);
                siteUpdateLog.setEnabled(true);
                siteUpdateLog.setCreateDt(new Date());
                siteUpdateLog.setModDt(new Date());
                if (siteService.saveAndPostUpdateLog(siteUpdateLog)) {
                    model.setInfoMessage("New site update created and posted.");
                    //set values back to blank.
                    model.setNewUpdateSubject("");
                    model.setNewUpdateText("");
                } else {
                    model.setErrorMessage("Failed to save and post new site update.");
                }
            }
        }
        headerModel.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/update", "siteAdminUpdateViewModel", model);
    }

    @GetMapping(UPDATE_MANAGE)
    public ModelAndView getSiteAdminUpdateManageView(SiteAdminUpdateViewModel model, Model headerModel) {
        headerModel.addAttribute("appVersion", getAppVersion());
        model.setSiteUpdateLogList(siteService.getSiteUpdateLogs());
        return new ModelAndView("site_admin/updateManage", "siteAdminUpdateViewModel", model);
    }

    @GetMapping(UPDATE_ENABLED)
    public ModelAndView getSiteAdminUpdateEnabled(@RequestParam(name = "id") long updateId) {

        SiteUpdateLog siteUpdateLog = siteService.getSiteUpdateLog(updateId);
        if (siteUpdateLog != null) {
            siteUpdateLog.setEnabled(!siteUpdateLog.getEnabled());
            siteUpdateLog.setModDt(new Date());
            siteService.saveUpdateLog(siteUpdateLog);
        }

        return new ModelAndView("redirect:" + UPDATE_MANAGE);
    }

    @GetMapping(UPDATE_EDIT)
    public ModelAndView getSiteAdminUpdateEdit(@RequestParam(name = "id") long updateId,
                                               SiteAdminUpdateViewModel model,
                                               Model headerModel) {
        SiteUpdateLog siteUpdateLog = siteService.getSiteUpdateLog(updateId);

        String updatePlainText = siteUpdateLog.getMessage();
        //convert new line HTML tags to actual new lines in the text box.
        updatePlainText = updatePlainText.replaceAll("<br />", "\r");

        model.setEditUpdateId(siteUpdateLog.getId());
        model.setEditUpdateSubject(siteUpdateLog.getSubject());
        model.setEditUpdateText(updatePlainText);

        headerModel.addAttribute("appVersion", getAppVersion());
        return new ModelAndView("site_admin/updateEdit", "siteAdminUpdateViewModel", model);
    }

    @PostMapping(UPDATE_EDIT)
    public ModelAndView postSiteAdminUpdateEdit(@ModelAttribute SiteAdminUpdateViewModel model,
                                                Model headerModel) {
        headerModel.addAttribute("appVersion", getAppVersion());
        if (model.getEditUpdateSubject() != null && !model.getEditUpdateSubject().trim().isEmpty()
                && model.getEditUpdateText() != null && !model.getEditUpdateText().trim().isEmpty()) {
            SiteUpdateLog siteUpdateLog = siteService.getSiteUpdateLog(model.getEditUpdateId());

            if (siteUpdateLog != null) {
                if (model.getPreviewButton() != null && !model.getPreviewButton().isEmpty()) {
                    model.setShowPreview(true);
                    model.setUpdatePreviewText(addHtmlToUpdateBody(model.getEditUpdateText()));
                    return new ModelAndView("site_admin/updateEdit", "siteAdminUpdateViewModel", model);
                } else {

                    //convert new lines to html <br /> tags.
                    String body = model.getEditUpdateText();
                    body = body.replaceAll("\r", "<br />");

                    siteUpdateLog.setSubject(model.getEditUpdateSubject());
                    siteUpdateLog.setMessage(body);
                    siteUpdateLog.setModDt(new Date());
                    siteService.saveUpdateLog(siteUpdateLog);
                }
            }
        }
        return new ModelAndView("redirect:" + UPDATE_MANAGE);
    }

    /**
     * add links to urls and add new lines.
     * @param body update body text to add new lines and links to
     * @return body with html added.
     */
    private String addHtmlToUpdateBody(String body) {
        if (body != null && !body.trim().isEmpty()) {
            body = body.replaceAll("\r", "<br />");
            body = inputHelper.addUrlHtmlLinksToString(body);
        }
        return body;
    }

    /**
     * Removes html added for new lines and links from update body.
     * @param body update body to remove new lines and links from
     * @return body with html tags replaced.
     */
    private String removeHtmlFromUpdateBody(String body) {
        if (body != null && !body.trim().isEmpty()) {
            body = body.replaceAll("<br />", "\r");
            body = body.replaceAll("<a target=\"_blank\" href=\".+?\">", "");
            body = body.replaceAll("</a>", "");
        }
        return body;
    }
}
