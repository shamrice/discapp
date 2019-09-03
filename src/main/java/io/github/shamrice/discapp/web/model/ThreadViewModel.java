package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.web.util.InputHelper;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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

}
