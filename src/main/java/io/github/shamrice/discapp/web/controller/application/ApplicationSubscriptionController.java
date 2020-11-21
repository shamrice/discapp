package io.github.shamrice.discapp.web.controller.application;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.ApplicationSubscriptionService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.define.url.ApplicationSubscriptionUrl;
import io.github.shamrice.discapp.web.model.applicationsubscription.ApplicationSubscriptionModel;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

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

    @Autowired
    private InputHelper inputHelper;

    @GetMapping(ApplicationSubscriptionUrl.UNSUBSCRIBE_URL)
    public ModelAndView getUnsubscribeView(@RequestParam(name = "id") long appId,
                                           @RequestParam(name = "email", required = false) String email,
                                           HttpServletRequest request) {

        ApplicationSubscriptionModel model = new ApplicationSubscriptionModel();
        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));
        model.setEmail(email);

        Application app = applicationService.get(appId);
        if (app != null) {

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        }

        return new ModelAndView("subscription/unsubscribe", "applicationSubscriptionModel", model);
    }

    @PostMapping(ApplicationSubscriptionUrl.UNSUBSCRIBE_URL)
    public ModelAndView postUnsubscribeView(@RequestParam(name = "id") long appId,
                                            @ModelAttribute ApplicationSubscriptionModel model,
                                            HttpServletRequest request) {

        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));

        Application app = applicationService.get(appId);
        if (app != null) {

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        }

        if (model.getEmail() != null && !model.getEmail().trim().isEmpty()) {
            try {
                applicationSubscriptionService.unsubscribeFromApplication(appId, model.getEmail());
                String unsubscribeHtml = configurationService.getStringValue(appId, ConfigurationProperty.MAILING_LIST_UNSUBSCRIBE_PAGE_HTML, "You are now unsubscribed from the mailing list.");
                model.setUnsubscribeHtml(unsubscribeHtml);
            } catch (Exception ex) {
                log.error("Failed to unsubscribe user: " + ex.getMessage(), ex);
                model.setUnsubscribeHtml("An error occurred. Please try again later.");
            }
        } else {
            model.setUnsubscribeHtml("Please enter a valid email address and try again.");
        }

        return new ModelAndView("subscription/unsubscribed", "applicationSubscriptionModel", model);
    }

    @GetMapping(ApplicationSubscriptionUrl.SUBSCRIBE_URL)
    public ModelAndView getSubscribeView(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "email", required = false) String email,
                                         @RequestParam(name = "encoded", required = false) Boolean emailEncoded,
                                         @RequestParam(name = "errorMessage", required = false) String errorMessage,
                                         HttpServletRequest request) {

        ApplicationSubscriptionModel model = new ApplicationSubscriptionModel();
        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));

        model.setErrorMessage(errorMessage);

        Application app = applicationService.get(appId);
        if (app != null) {

            if (email != null && !email.trim().isEmpty()) {

                //decode email if it was encoded when passed so it looks correct to the user.
                if (emailEncoded != null && emailEncoded) {
                    email = UriUtils.decode(email, StandardCharsets.UTF_8);
                }
                model.setEmail(email);
            }

            model.setReturnToApplicationText("Return to " + app.getName());

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));

            model.setSubscribeButtonText("Subscribe");
            model.setSubscriptionEmailTextBoxLabel(
                    configurationService.getStringValue(
                            app.getId(), ConfigurationProperty.MAILING_LIST_DESCRIPTION_PAGE_HTML,
                            "Enter your email address in order to receive daily updates. ")
            );
        }

        return new ModelAndView("subscription/subscribe", "applicationSubscriptionModel", model);
    }

    /**
     * Create subscription request and redirect user to follow up page. Email is generated with subscription request.
     * @param appId
     * @param applicationSubscriptionModel
     * @param request
     * @return
     */
    @PostMapping(ApplicationSubscriptionUrl.SUBSCRIBE_URL)
    public ModelAndView postSubscribeView(@RequestParam(name = "id") long appId,
                                          @ModelAttribute ApplicationSubscriptionModel applicationSubscriptionModel,
                                          HttpServletRequest request) {

        String baseUrl = webHelper.getBaseUrl(request);
        applicationSubscriptionModel.setBaseUrl(baseUrl);
        applicationSubscriptionModel.setApplicationId(appId);

        Application app = applicationService.get(appId);
        if (app != null) {

            //verify recaptcha
            if (!inputHelper.verifyReCaptchaResponse(applicationSubscriptionModel.getReCaptchaResponse())) {
                log.warn("Failed to create subscription request for " + applicationSubscriptionModel.getEmail()
                        + " due to ReCaptcha verification failure.");
                String errorMessage = "Failed to create subscription request. Please try again.";
                return getSubscribeView(appId, null, null, errorMessage, request);
            }

            applicationSubscriptionModel.setReturnToApplicationText("Return to " + app.getName());

            applicationSubscriptionModel.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            applicationSubscriptionModel.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));

            //check to see if the user is already subscribed before resubscribing them.
            if (applicationSubscriptionService.isEmailAlreadySubscribed(app.getId(), applicationSubscriptionModel.getEmail())) {
                log.info("Email address: " + applicationSubscriptionModel.getEmail() + " is already subscribed to appId: " + appId);
                applicationSubscriptionModel.setFollowUpHtml("You are already subscribed to this mailing list.");

            } else {
                //subscribe user.
                applicationSubscriptionModel.setFollowUpHtml(configurationService.getStringValue(
                        app.getId(), ConfigurationProperty.MAILING_LIST_FOLLOW_UP_PAGE_HTML,
                        "A confirmation message has been sent to your address."));

                //make sure email address attempted was not blank before continuing.
                if (applicationSubscriptionModel.getEmail() != null && !applicationSubscriptionModel.getEmail().isEmpty()) {

                    //uri encode email for confirm url
                    String urlEmail = UriUtils.encode(applicationSubscriptionModel.getEmail(), StandardCharsets.UTF_8);
                    //code query param value will be added by service.
                    String confirmUrl = baseUrl + ApplicationSubscriptionUrl.CONFIRM_URL + "?id=" + app.getId()
                            + "&email=" + urlEmail + "&code=";

                    String confirmationMessage = configurationService.getStringValue(
                            app.getId(), ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_MESSAGE,
                            "Please click on the link below to confirm your subscription.");

                    try {
                        applicationSubscriptionService.createSubscriptionRequest(app.getId(), app.getName(), confirmUrl,
                                confirmationMessage, applicationSubscriptionModel.getEmail());

                    } catch (Exception ex) {
                        log.error("Failed to subscribe user: " + ex.getMessage(), ex);
                        applicationSubscriptionModel.setSubscriptionResponseMessage("An error occurred. Please try again later.");
                    }
                } else {
                    log.warn("Invalid email address attempted to be subscribed to appId: " + appId);
                }
            }
        }

        return new ModelAndView("subscription/followup", "applicationSubscriptionModel", applicationSubscriptionModel);
    }

    /**
     * Mailing list confirmation page that gets linked in email sent out.
     * @param appId
     * @param email
     * @param confirmationCode
     * @param request
     * @return
     */
    @GetMapping(ApplicationSubscriptionUrl.CONFIRM_URL)
    public ModelAndView getConfirmationView(@RequestParam(name = "id") long appId,
                                           @RequestParam(name = "email") String email,
                                           @RequestParam(name = "code") int confirmationCode,
                                           HttpServletRequest request) {

        ApplicationSubscriptionModel model = new ApplicationSubscriptionModel();
        model.setApplicationId(appId);
        model.setBaseUrl(webHelper.getBaseUrl(request));
        model.setEmail(email);

        Application app = applicationService.get(appId);
        if (app != null) {
            model.setReturnToApplicationText("Return to " + app.getName());

            model.setApplicationStyleSheetUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

            model.setApplicationFaviconUrl(configurationService.getStringValue(
                    app.getId(), ConfigurationProperty.FAVICON_URL, "/favicon.ico"));

            if (email != null && !email.trim().isEmpty()) {
                try {
                    if (applicationSubscriptionService.subscribeToApplication(app.getId(), email, confirmationCode)) {
                        String confirmationHtml = configurationService.getStringValue(appId, ConfigurationProperty.MAILING_LIST_CONFIRMATION_PAGE_HTML, "Your are now subscribed. Your will receive updates when new articles are posted.");
                        model.setConfirmationHtml(confirmationHtml);
                    } else {
                        model.setConfirmationHtml("Failed to confirm subscription. Please try again.");
                    }
                } catch (Exception ex) {
                    log.error("Failed to confirm subscription for user: " + ex.getMessage(), ex);
                    model.setSubscriptionResponseMessage("An error occurred. Please try again later.");
                }
            }
        }
        return new ModelAndView("subscription/confirm", "applicationSubscriptionModel", model);
    }

}
