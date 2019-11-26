package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceMailingListViewModel {

    public static final String FORMS_TAB = "Forms";
    public static final String APPEARANCE_TAB = "Appearance";
    public static final String SUBSCRIBERS_TAB = "Subscribers";

    private long applicationId;
    private String infoMessage;

    private String currentTab;

    private String subscribeUrl;
    private String unsubscribeUrl;

    private String subscribeHtmlForm;
    private String unsubscribeHtmlForm;
}
