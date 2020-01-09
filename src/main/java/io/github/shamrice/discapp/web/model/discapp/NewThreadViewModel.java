package io.github.shamrice.discapp.web.model.discapp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class NewThreadViewModel {

    private String appId;
    private String submitter;
    private String email;
    private String ipAddress;
    private String subject;
    private String body;
    private String parentId;
    private String submitNewThread;
    private String returnToApp;
    private String previewArticle;
    private boolean showEmail;
    private boolean isLoggedIn;
    private String htmlBody;
    private String parentThreadSubmitter;
    private String parentThreadSubject;
    private String parentThreadBody;
    private Integer currentPage;
    private String errorMessage;
    private String subscribe;

}
