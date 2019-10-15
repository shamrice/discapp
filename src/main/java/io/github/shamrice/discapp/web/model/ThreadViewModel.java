package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ThreadViewModel {

    private String id;
    private String appId;
    private String submitter;
    private String currentUsername;
    private String email;
    private String ipAddress;
    private String subject;
    private String body;
    private String parentId;
    private String createDt;
    private String modDt;
    private String returnToApp;
    private String postResponse;
    private String previewText;
    private boolean isShowMoreOnPreviewText;
    private boolean showEmail;
    private boolean isShowIpAddress;
    private Integer currentPage;

}
