package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.web.define.url.ApplicationSubscriptionUrl;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceMailingListViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.LIST_MAINTENANCE_PAGE;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.LIST_MAINTENANCE_PREVIEW_PAGE;
import static io.github.shamrice.discapp.web.model.maintenance.MaintenanceMailingListViewModel.*;

@Controller
@Slf4j
public class MailingListMaintenanceController extends MaintenanceController {

    private static final String EMAIL_UPDATE_ALL = "all";
    private static final String EMAIL_UPDATE_ALL_PREVIEW = "allPreview";
    private static final String EMAIL_UPDATE_FIRST = "first";
    private static final String EMAIL_UPDATE_FIRST_PREVIEW = "preview";

    @GetMapping(LIST_MAINTENANCE_PAGE)
    public ModelAndView getListMaintenanceView(@RequestParam(name = "id") long appId,
                                               @RequestParam(name = "tab", required = false) String tab,
                                               @RequestParam(name = "status", required =  false) String status,
                                               HttpServletRequest request,
                                               Model model,
                                               MaintenanceMailingListViewModel listViewModel) {

        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);

        if (listViewModel == null) {
            listViewModel = new MaintenanceMailingListViewModel();
        }

        if (status != null && !status.isEmpty()) {
            listViewModel.setInfoMessage(status);
        }

        if (tab == null || tab.trim().isEmpty()) {
            tab = FORMS_TAB;
        }
        listViewModel.setCurrentTab(tab);

        listViewModel.setApplicationId(app.getId());

        if (tab.equalsIgnoreCase(FORMS_TAB)) {

            String baseUrl = webHelper.getBaseUrl(request);
            String subscribeUrl = baseUrl + ApplicationSubscriptionUrl.SUBSCRIBE_URL;
            String unsubscribeUrl = baseUrl + ApplicationSubscriptionUrl.UNSUBSCRIBE_URL;

            listViewModel.setSubscribeUrl(subscribeUrl);
            listViewModel.setUnsubscribeUrl(unsubscribeUrl);

            String subscribeFormHtml = "" +
                    "<FORM METHOD=\"POST\" ACTION=\"SUBSCRIBE_URL\">\n" +
                    "<INPUT TYPE=\"text\" NAME=\"email\" SIZE=40>\n" +
                    "<INPUT TYPE=\"hidden\" NAME=\"id\" VALUE=APPLICATION_ID>\n" +
                    "<INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Subscribe\">\n" +
                    "</FORM>";
            subscribeFormHtml = subscribeFormHtml
                    .replace("SUBSCRIBE_URL", subscribeUrl)
                    .replace("APPLICATION_ID", app.getId().toString());

            String unsubscribeFormHtml = "<FORM METHOD=\"POST\" ACTION=\"UNSUBSCRIBE_URL\">\n" +
                    "<INPUT TYPE=\"text\" NAME=\"email\" SIZE=40>\n" +
                    "<INPUT TYPE=\"hidden\" NAME=\"id\" VALUE=APPLICATION_ID>\n" +
                    "<INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Unsubscribe\">\n" +
                    "</FORM>";
            unsubscribeFormHtml = unsubscribeFormHtml
                    .replace("UNSUBSCRIBE_URL", unsubscribeUrl)
                    .replace("APPLICATION_ID", app.getId().toString());

            listViewModel.setSubscribeHtmlForm(subscribeFormHtml);
            listViewModel.setUnsubscribeHtmlForm(unsubscribeFormHtml);

            listViewModel.setEmailUpdateSetting(configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_EMAIL_UPDATE_SETTINGS, "all"));

        } else if (tab.equalsIgnoreCase(APPEARANCE_TAB)) {

            //if not coming back from preview, pull from config. otherwise values already passed in model.
            if (listViewModel.getUpdateFormsEditButton() == null) {
                String descriptionText = configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_DESCRIPTION_PAGE_HTML, "Enter your email address in order to receive daily updates.");
                String followUpPageText = configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_FOLLOW_UP_PAGE_HTML, "A confirmation message has been sent to your address.");
                String confirmationMessage = configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_MESSAGE, "Please click on the link below to confirm your subscription.");
                String confirmationPageText = configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_CONFIRMATION_PAGE_HTML, "You are now subscribed. You will receive updates when new articles are posted. ");
                String unsubscribePageText = configurationService.getStringValue(app.getId(), ConfigurationProperty.MAILING_LIST_UNSUBSCRIBE_PAGE_HTML, "You are unsubscribed from this mailing list.");

                listViewModel.setDescriptionText(descriptionText);
                listViewModel.setFollowUpPageText(followUpPageText);
                listViewModel.setConfirmationMessageText(confirmationMessage);
                listViewModel.setConfirmationPageText(confirmationPageText);
                listViewModel.setUnsubscribePageText(unsubscribePageText);
            }

        } else if (tab.equalsIgnoreCase(SUBSCRIBERS_TAB)) {

            List<ApplicationSubscription> subscriptions = applicationSubscriptionService.getSubscribers(app.getId());

            if (subscriptions != null) {
                List<MaintenanceMailingListViewModel.Subscriber> subscribers = new ArrayList<>();
                for (ApplicationSubscription subscription : subscriptions) {
                    MaintenanceMailingListViewModel.Subscriber subscriber = new MaintenanceMailingListViewModel.Subscriber(subscription.getSubscriberEmail(), subscription.getModDt());
                    subscribers.add(subscriber);
                }
                listViewModel.setSubscribers(subscribers);
            }
        } else {

            String emailReplySetting = "off";
            boolean emailReplySettingValue = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.EMAIL_REPLY_NOTIFICATION_ENABLED, false);
            if (emailReplySettingValue) {
                emailReplySetting = "on";
            }

            listViewModel.setEmailReplySetting(emailReplySetting);
        }

        return new ModelAndView("admin/disc-list-maint", "maintenanceMailingListViewModel", listViewModel);
    }

    @PostMapping(LIST_MAINTENANCE_PAGE)
    public ModelAndView postListMaintenanceView(@RequestParam(name = "id") long appId,
                                                @RequestParam(name = "tab", required = false) String tab,
                                                @ModelAttribute MaintenanceMailingListViewModel listViewModel,
                                                HttpServletRequest request,
                                                Model model) {
        String status = "Saved.";

        //preview button clicked, show preview page
        if (listViewModel.getUpdateFormsPreviewButton() != null && !listViewModel.getUpdateFormsPreviewButton().isEmpty()) {
            return getListPreviewView(appId, tab, listViewModel, request, model);
        }

        //return from preview button clicked, show get appearance tab on main template.
        if (listViewModel.getUpdateFormsEditButton() != null && !listViewModel.getUpdateFormsEditButton().isEmpty()) {
            return getListMaintenanceView(appId, tab, null, request, model, listViewModel);
        }

        if (listViewModel.getChangeBehaviorButton() != null && !listViewModel.getChangeBehaviorButton().isEmpty()) {
            boolean emailUpdateSettingsSaved = false;
            String emailUpdateSetting = listViewModel.getEmailUpdateSetting();
            if (emailUpdateSetting.equals(EMAIL_UPDATE_ALL) || emailUpdateSetting.equals(EMAIL_UPDATE_ALL_PREVIEW)
                    || emailUpdateSetting.equals(EMAIL_UPDATE_FIRST) || emailUpdateSetting.equals(EMAIL_UPDATE_FIRST_PREVIEW)) {
                if (configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_EMAIL_UPDATE_SETTINGS, listViewModel.getEmailUpdateSetting())) {
                    log.info("Updated email update settings for appId: " + appId + " to: " + listViewModel.getEmailUpdateSetting());
                    emailUpdateSettingsSaved = true;
                }
            }

            if (!emailUpdateSettingsSaved) {
                log.warn("Failed to update mailing list email settings for appId: " + appId
                        + " : Value attempted: " + emailUpdateSetting);
                status = "Failed to save mailing list email settings.";
            }
        }

        if (listViewModel.getUpdateFormsButton() != null && !listViewModel.getUpdateFormsButton().isEmpty()) {
            boolean descriptionSaved = configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_DESCRIPTION_PAGE_HTML, listViewModel.getDescriptionText());
            boolean followUpPageSaved = configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_FOLLOW_UP_PAGE_HTML, listViewModel.getFollowUpPageText());
            boolean confirmationMessageSaved = configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_MESSAGE, listViewModel.getConfirmationMessageText());
            boolean confirmationPageSaved = configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_CONFIRMATION_PAGE_HTML, listViewModel.getConfirmationPageText());
            boolean unsubscribePageSaved = configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.MAILING_LIST_UNSUBSCRIBE_PAGE_HTML, listViewModel.getUnsubscribePageText());

            if (!(descriptionSaved && followUpPageSaved && confirmationMessageSaved && confirmationPageSaved && unsubscribePageSaved)) {
                status = "Failed to save one more of mailing list appearance forms.";
            }
        }

        if (listViewModel.getChangeReplyBehaviorButton() != null && !listViewModel.getChangeReplyBehaviorButton().isEmpty()) {
            boolean replyNotificationEnabled = "on".equalsIgnoreCase(listViewModel.getEmailReplySetting());
            if (configurationService.saveApplicationConfiguration(appId, ConfigurationProperty.EMAIL_REPLY_NOTIFICATION_ENABLED, String.valueOf(replyNotificationEnabled))) {
                log.info("Updated reply notification settings for appId: " + appId + " to: " + replyNotificationEnabled);
            } else {
                log.warn("Failed to update email reply notification settings for appId: " + appId);
                status = "Failed to save reply notification settings.";
            }
        }

        return new ModelAndView("redirect:" + LIST_MAINTENANCE_PAGE + "?id=" + appId + "&status=" + status
                + "&tab=" + tab, "maintenanceMailingListViewModel", listViewModel);

    }

    @GetMapping(LIST_MAINTENANCE_PREVIEW_PAGE)
    public ModelAndView getListPreviewView(@RequestParam(name = "id") long appId,
                                                @RequestParam(name = "tab", required = false) String tab,
                                                @ModelAttribute MaintenanceMailingListViewModel listViewModel,
                                                HttpServletRequest request,
                                                Model model) {
        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);
        //tab can only be from appearance page.
        listViewModel.setCurrentTab(APPEARANCE_TAB);
        listViewModel.setApplicationId(appId);
        return new ModelAndView("admin/disc-list-preview", "maintenanceMailingListViewModel", listViewModel);
    }
}
