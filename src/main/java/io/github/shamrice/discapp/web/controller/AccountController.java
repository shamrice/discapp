package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.web.model.NewAccountViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class AccountController {

    Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @GetMapping("/account/create")
    public ModelAndView getCreateAccount(@ModelAttribute NewAccountViewModel newAccountViewModel,
                                         @RequestParam(required = false) String redirect,
                                         ModelMap modelMap) {

        modelMap.addAttribute("redirectUrl", redirect);
        return new ModelAndView("account/createAccount", "newAccountViewModel", newAccountViewModel);
    }

    @PostMapping("/account/create")
    public ModelAndView postCreateAccount(@ModelAttribute NewAccountViewModel newAccountViewModel, ModelMap modelMap) {

        if (newAccountViewModel != null) {
            logger.info("Attempting to create new account with username: " + newAccountViewModel.getUsername());

            //gross hack to remove html tags in subject, submitter and email fields if they exist.
            /* TODO : use below to reject any input that attempts to contain HTML
            String subject = newThreadViewModel.getSubject().replaceAll("<[^>]*>", " ");
            String submitter = newThreadViewModel.getSubmitter().replaceAll("<[^>]*>", " ");
            String email = newThreadViewModel.getEmail().replaceAll("<[^>]*>", " ");
            */

            //TODO : much more error checking
            if (newAccountViewModel.getPassword().length() < 8) {
                newAccountViewModel.setErrorMessage("Passwords must be at least eight characters in length.");
                return new ModelAndView("account/createAccount", "newAccountViewModel", newAccountViewModel);
            }

            DiscAppUser newUser = new DiscAppUser();
            newUser.setUsername(newAccountViewModel.getUsername());
            newUser.setPassword(newAccountViewModel.getPassword());
            newUser.setAdmin(newAccountViewModel.isAdmin());
            newUser.setEmail(newAccountViewModel.getEmail());
            newUser.setShowEmail(newAccountViewModel.isShowEmail());
            newUser.setEnabled(newAccountViewModel.isEnabled());
            newUser.setOwnerId(newAccountViewModel.getOwnerId());
            newUser.setModDt(new Date());
            newUser.setCreateDt(new Date());

            if (discAppUserDetailsService.saveDiscAppUser(newUser)) {
                logger.info("Successfully created new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());
                return new ModelAndView("account/createAccountSuccess");
            } else {
                logger.error("Failed to create new user: " + newUser.getUsername() + " : email: " + newUser.getEmail());
                newAccountViewModel.setErrorMessage("Failed to create new account for user: " + newAccountViewModel.getUsername());
            }
        }

        return new ModelAndView("account/createAccount", "newAccountViewModel", newAccountViewModel);

    }

    @GetMapping
    public String getCreateSuccess(ModelMap modelMap) {
        modelMap.addAttribute("status", "Successfully created new user");
        return "account/createAccountSuccess";
    }
}
