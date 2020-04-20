package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Owner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_DELETE;
import static io.github.shamrice.discapp.web.define.url.AccountUrl.ACCOUNT_DELETE_STATUS;

@Controller
@Slf4j
public class AccountDeleteController extends AccountController {


    @GetMapping(ACCOUNT_DELETE_STATUS)
    public ModelAndView getAccountDeleteStatus(ModelMap modelMap) {
        return new ModelAndView("account/delete/deleteAccountStatus", modelMap);
    }

    @GetMapping(ACCOUNT_DELETE)
    public ModelAndView getAccountDelete(ModelMap modelMap) {
        return new ModelAndView("account/delete/deleteAccount", modelMap);
    }

    @PostMapping(ACCOUNT_DELETE)
    public ModelAndView postAccountDelete(@RequestParam(name = "reCaptchaResponse") String reCaptchaResponse,
                                          ModelMap modelMap, HttpSession session) {

        if (!inputHelper.verifyReCaptchaResponse(reCaptchaResponse)) {
            log.warn("Delete account reCaptcha verification failed.");
            modelMap.addAttribute("status", "User deletion failed.");
            return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
        }

        String username = accountHelper.getLoggedInEmail();
        if (username != null) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(username);
            if (user != null) {

                //mark owner as disabled and deleted. (when owner is disabled, all associated apps will be as well)
                if (user.getOwnerId() != null) {
                    Owner appOwner = accountService.getOwnerById(user.getOwnerId());
                    if (appOwner != null) {
                        appOwner.setEmail(appOwner.getEmail() + "_DELETED_" + UUID.randomUUID().toString());
                        appOwner.setEnabled(false);
                        log.info("Delete Account : Disabling account owner id: " + appOwner.toString());
                        if (accountService.saveOwner(appOwner) != null) {
                            log.info("Successfully marked owner " + appOwner.toString() + " as disabled.");
                        } else {
                            log.error("Failed to mark owner: " + appOwner.toString() + " as disabled.");
                        }

                        //mark apps as deleted as well.
                        List<Application> apps = applicationService.getByOwnerId(appOwner.getId());
                        for (Application app : apps) {
                            app.setEnabled(false);
                            app.setDeleted(true);
                            app.setSearchable(false);
                            app.setModDt(new Date());
                            applicationService.save(app);
                            log.info("Marked application id: " + app.getId()
                                    + " as deleted due to account deletion of: " + user.getEmail());
                        }
                    }
                }

                //mark disc app account as disabled
                //todo : this should also turn username and email into a UUID so email/username can be reused by someone else
                if (discAppUserDetailsService.deactivateUser(user.getId())) {
                    log.info("Successfully deactivated disc app user: " + user.toString());
                    modelMap.addAttribute("status", "User successfully deleted.");

                    //invalidate current session.
                    session.invalidate();

                    return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
                } else {
                    log.error("Failed to disable disc app user: " + user.toString());
                }
            }
        }
        modelMap.addAttribute("status", "User deletion failed.");
        return new ModelAndView("account/delete/deleteAccountStatus", "model", modelMap);
    }

}
