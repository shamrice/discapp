package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.ImportData;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.repository.*;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
public class SiteAdminController {

    public static final String CONTROLLER_URL_DIRECTORY = "/site_admin/";

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

    @GetMapping(CONTROLLER_URL_DIRECTORY)
    public ModelAndView getSiteAdmin(ModelAndView model) {
        return new ModelAndView("site_admin/admin", "model", model);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "accounts")
    public ModelAndView getSiteAdminAccounts(SiteAdminAccountViewModel siteAdminAccountViewModel,
                                             Model model) {
        List<DiscAppUser> fullUserList = discAppUserRepository.findAll();
        fullUserList.sort((u1, u2) -> {
            if (u1.getId().equals(u2.getId())) {
                return 0;
            }
            return u1.getId() > u2.getId() ? 1 : -1;
        });
        siteAdminAccountViewModel.setUserList(fullUserList);
        return new ModelAndView("site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "account/showEmail")
    public ModelAndView getSiteAdminAccountShowEmail(@RequestParam(name = "id") Long userId,
                                                     @RequestParam(name = "enabled") Boolean enabled,
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
        return new ModelAndView("redirect:/site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "account/enabled")
    public ModelAndView getSiteAdminAccountEnabled(@RequestParam(name = "id") Long userId,
                                                     @RequestParam(name = "enabled") Boolean enabled,
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
        return new ModelAndView("redirect:/site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "account/isAdmin")
    public ModelAndView getSiteAdminIsAdmin(@RequestParam(name = "id") Long userId,
                                                   @RequestParam(name = "enabled") Boolean enabled,
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
        return new ModelAndView("redirect:/site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "account/isUserAccount")
    public ModelAndView getSiteAdminIsUserAccount(@RequestParam(name = "id") Long userId,
                                                   @RequestParam(name = "enabled") Boolean enabled,
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
        return new ModelAndView("redirect:/site_admin/accounts", "siteAdminAccountViewModel", siteAdminAccountViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "owner")
    public ModelAndView getSiteAdminOwnerInfo(@RequestParam(name = "ownerId") Long ownerId,
                                              SiteAdminOwnerViewModel siteAdminOwnerViewModel,
                                              Model model) {
        try {
            Owner owner = ownerRepository.getOne(ownerId);
            siteAdminOwnerViewModel.setOwner(owner);
        } catch (Exception ex) {
            log.error("Failed to get owner with id: " + ownerId + " :: " + ex.getMessage(), ex);
            siteAdminOwnerViewModel.setErrorMessage("Unable to find owner with id: " + ownerId);
        }
        return new ModelAndView("site_admin/owner", "siteAdminOwnerViewModel", siteAdminOwnerViewModel);

    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "applications")
    public ModelAndView getSiteAdminApplications(SiteAdminApplicationViewModel siteAdminApplicationViewModel,
                                                 Model model) {
        List<Application> fullApplicationList = applicationRepository.findAll();
        fullApplicationList.sort((u1, u2) -> {
            if (u1.getId().equals(u2.getId())) {
                return 0;
            }
            return u1.getId() > u2.getId() ? 1 : -1;
        });
        siteAdminApplicationViewModel.setApplicationList(fullApplicationList);
        return new ModelAndView("site_admin/applications", "siteAdminApplicationViewModel", siteAdminApplicationViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "application/enabled")
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
        return new ModelAndView("redirect:/site_admin/applications", "siteAdminApplicationViewModel", siteAdminApplicationViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "application/deleted")
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
        return new ModelAndView("redirect:/site_admin/applications", "siteAdminApplicationViewModel", siteAdminApplicationViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "application/isSearchable")
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
        return new ModelAndView("redirect:/site_admin/applications", "siteAdminApplicationViewModel", siteAdminApplicationViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "imports")
    public ModelAndView getSiteAdminImports(SiteAdminImportViewModel siteAdminImportViewModel,
                                            Model model) {
        List<ImportData> fullImportDataList = importDataRepository.findAll();
        fullImportDataList.sort((u1, u2) -> {
            if (u1.getId().equals(u2.getId())) {
                return 0;
            }
            return u1.getId() > u2.getId() ? 1 : -1;
        });
        siteAdminImportViewModel.setImportDataList(fullImportDataList);
        return new ModelAndView("site_admin/imports", "siteAdminImportViewModel", siteAdminImportViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "import/delete")
    public ModelAndView getSiteAdminImportDelete(@RequestParam(name = "id") Long importId,
                                                 SiteAdminImportViewModel siteAdminImportViewModel) {

        try {
            log.info("Deleting import id: " + importId);
            importDataRepository.deleteById(importId);
        } catch (Exception ex) {
            log.error("Failed to delete import id: " + importId);
        }
        return new ModelAndView("redirect:/site_admin/imports", "siteAdminImportViewModel", siteAdminImportViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "import/download")
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

    @GetMapping(CONTROLLER_URL_DIRECTORY + "threads")
    public ModelAndView getSiteAdminThreads(SiteAdminThreadViewModel siteAdminThreadViewModel,
                                            Model model) {
        List<Thread> fullThreadList = threadService.getAllDeletedThreads();
        /*
        fullImportDataList.sort((u1, u2) -> {
            if (u1.getId().equals(u2.getId())) {
                return 0;
            }
            return u1.getId() > u2.getId() ? 1 : -1;
        });

         */
        siteAdminThreadViewModel.setThreadList(fullThreadList);
        return new ModelAndView("site_admin/threads", "siteAdminThreadViewModel", siteAdminThreadViewModel);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "thread/restore")
    public ModelAndView getSiteAdminThreadRestore(@RequestParam(name = "id") Long threadId,
                                                 SiteAdminThreadViewModel siteAdminThreadViewModel) {

        try {
            log.info("Restoring thread id: " + threadId);
            Thread thread = threadService.getThreadById(threadId);
            thread.setDeleted(false);
            thread.setModDt(new Date());
            if (threadService.saveThread(thread, thread.getBody()) > 0) {
                log.info("Thread id: " + threadId + " restored successfully.");
            } else {
                log.warn("Failed to restore thread id: " + threadId);
            }
        } catch (Exception ex) {
            log.error("Failed to restore thread id: " + threadId + " :: " + ex.getMessage(), ex);
        }
        return new ModelAndView("redirect:/site_admin/threads", "siteAdminThreadViewModel", siteAdminThreadViewModel);
    }
}
