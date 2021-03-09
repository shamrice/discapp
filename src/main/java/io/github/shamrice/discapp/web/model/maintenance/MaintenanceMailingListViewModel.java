package io.github.shamrice.discapp.web.model.maintenance;

import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceMailingListViewModel {

    public static final String FORMS_TAB = "Forms";
    public static final String APPEARANCE_TAB = "Appearance";
    public static final String SUBSCRIBERS_TAB = "Subscribers";
    public static final String REPLY_TAB = "Reply";

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Subscriber {
        @NonNull
        private String email;

        @NonNull
        private Date subscribeDate;
    }

    private long applicationId;
    private String infoMessage;

    private String currentTab;

    private String subscribeUrl;
    private String unsubscribeUrl;

    private String subscribeHtmlForm;
    private String unsubscribeHtmlForm;
    private String changeBehaviorButton;
    private String emailUpdateSetting;

    private List<Subscriber> subscribers = new ArrayList<>();

    private String descriptionText;
    private String followUpPageText;
    private String confirmationMessageText;
    private String confirmationPageText;
    private String unsubscribePageText;
    private String updateFormsButton;
    private String updateFormsPreviewButton;
    private String updateFormsEditButton;

    private String emailReplySetting;
    private String changeReplyBehaviorButton;

    public int getSubscriberCount() {
        return this.subscribers.size();
    }

    public boolean isEmailUpdateSettingChecked(String value) {
        if (value == null || value.isEmpty())
            return false;
        if (emailUpdateSetting == null || emailUpdateSetting.isEmpty()) {
            return false;
        }
        return emailUpdateSetting.equalsIgnoreCase(value);
    }

    public boolean isEmailReplySettingChecked(String value) {
        if (value == null || value.isEmpty())
            return false;
        if (emailReplySetting == null || emailReplySetting.isEmpty()) {
            return false;
        }
        return emailReplySetting.equalsIgnoreCase(value);
    }
}
