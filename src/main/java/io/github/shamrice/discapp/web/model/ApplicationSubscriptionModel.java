package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationSubscriptionModel {

    private String subscriptionEmailTextBoxLabel;
    private String subscribeButtonText;
    private String returnToApplicationText;
    private long applicationId;
    private String email;
    private String subscribe;
    private String applicationStyleSheetUrl;
    private String applicationFaviconUrl;
    private String baseUrl;

    private String subscriptionResponseMessage;
    private String unsubscribeHtml;
    private String followUpHtml;
    private String confirmationHtml;

}
