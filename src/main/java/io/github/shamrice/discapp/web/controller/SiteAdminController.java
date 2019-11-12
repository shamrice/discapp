package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import io.github.shamrice.discapp.web.model.SiteAdminAccountViewModel;
import io.github.shamrice.discapp.web.model.SiteAdminOwnerViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
public class SiteAdminController {

    public static final String CONTROLLER_URL_DIRECTORY = "/site_admin/";

    @Autowired
    private DiscAppUserRepository discAppUserRepository;

    @Autowired
    private OwnerRepository ownerRepository;

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
}
