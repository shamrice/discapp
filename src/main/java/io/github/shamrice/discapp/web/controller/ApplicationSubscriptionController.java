package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.ApplicationSubscriptionService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.define.url.ApplicationSubscriptionUrl;
import io.github.shamrice.discapp.web.model.ApplicationSubscriptionModel;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class ApplicationSubscriptionController {

    @Autowired
    private ApplicationSubscriptionService applicationSubscriptionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private WebHelper webHelper;


    //TODO : should ask to verify subscriptions with email message to confirm.

    @GetMapping(ApplicationSubscriptionUrl.UNSUBSCRIBE_URL)
    public ModelAndView getUnsubscribeView(@RequestParam(name = "id") long appId,
                                           @RequestParam(name = "email") String email,
                                           HttpServletRequest request) {

        ApplicationSubscriptionModel model = new ApplicationSubscriptionModel();
        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));

        Application app = applicationService.get(appId);
        if (app != null) {
            model.setReturnToApplicationText("Return to " + app.getName());

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        }

        try {
            applicationSubscriptionService.unsubscribeFromApplication(appId, email);
            model.setSubscriptionResponseMessage("You have been successfully unsubscribed.");
        } catch (Exception ex) {
            log.error("Failed to unsubscribe user: " + ex.getMessage(), ex);
            model.setSubscriptionResponseMessage("An error occurred. Please try again later.");
        }

        return new ModelAndView("subscription/subscriptionResponse", "applicationSubscriptionModel", model);
    }

    @GetMapping(ApplicationSubscriptionUrl.SUBSCRIBE_URL)
    public ModelAndView getSubscribeView(@RequestParam(name = "id") long appId,
                                         HttpServletRequest request) {

        ApplicationSubscriptionModel model = new ApplicationSubscriptionModel();
        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));

        Application app = applicationService.get(appId);
        if (app != null) {
            model.setReturnToApplicationText("Return to " + app.getName());

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));

            //todo : pull these from app configuration.
            model.setSubscribeButtonText("Subscribe");
            model.setSubscriptionEmailTextBoxLabel("Enter your email address in order to receive daily updates.");
        }

        return new ModelAndView("subscription/subscribe", "applicationSubscriptionModel", model);
    }

    @PostMapping(ApplicationSubscriptionUrl.SUBSCRIBE_URL)
    public ModelAndView postSubscribeView(@RequestParam(name = "id") long appId,
                                          @ModelAttribute ApplicationSubscriptionModel applicationSubscriptionModel,
                                          HttpServletRequest request) {


        applicationSubscriptionModel.setBaseUrl(webHelper.getBaseUrl(request));
        applicationSubscriptionModel.setApplicationId(appId);

        Application app = applicationService.get(appId);
        if (app != null) {
            applicationSubscriptionModel.setReturnToApplicationText("Return to " + app.getName());

            applicationSubscriptionModel.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            applicationSubscriptionModel.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));

            try {
                applicationSubscriptionService.subscribeToApplication(app.getId(), applicationSubscriptionModel.getEmail());
                //todo : get from app config.
                applicationSubscriptionModel.setSubscriptionResponseMessage("You have been successfully subscribed.");
            } catch (Exception ex) {
                log.error("Failed to subscribe user: " + ex.getMessage(), ex);
                applicationSubscriptionModel.setSubscriptionResponseMessage("An error occurred. Please try again later.");
            }
        }

        return new ModelAndView("subscription/subscriptionResponse", "applicationSubscriptionModel", applicationSubscriptionModel);
    }

    @PostMapping(ApplicationSubscriptionUrl.UNSUBSCRIBE_URL)
    public ModelAndView postUnsubscribeView(@RequestParam(name = "id") long appId,
                                            @ModelAttribute ApplicationSubscriptionModel model,
                                            HttpServletRequest request) {

        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));

        Application app = applicationService.get(appId);
        if (app != null) {
            model.setReturnToApplicationText("Return to " + app.getName());

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        }

        try {
            applicationSubscriptionService.unsubscribeFromApplication(appId, model.getEmail());
            model.setSubscriptionResponseMessage("You have been successfully unsubscribed.");
        } catch (Exception ex) {
            log.error("Failed to unsubscribe user: " + ex.getMessage(), ex);
            model.setSubscriptionResponseMessage("An error occurred. Please try again later.");
        }

        return new ModelAndView("subscription/subscriptionResponse", "applicationSubscriptionModel", model);
    }

}
