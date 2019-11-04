package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.ReportedAbuse;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AbuseViewModel {

    @RequiredArgsConstructor
    @Getter
    public static class ReportedThread {

        private @NonNull long applicationId;
        private @NonNull Date threadCreateDt;
        private @NonNull String ipAddress;
        private @NonNull String submitter;
        private @NonNull String emailAddress;
        private @NonNull String subject;
        private @NonNull long threadId;
        private @NonNull boolean isDeletable;

        @Setter
        private String deleteUrlQueryParameter;

    }

    private String infoMessage;
    private String errorMessage;

    private long discAppId;
    private long threadId;
    private String submitter;
    private String email;
    private String ipAddress;
    private Date createDt;
    private String subject;
    private String message;
    private String whoIsUrl;
    private List<ReportedThread> reportedThreads = new ArrayList<>();

    private String searchForm;
}
