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

    }

    private String infoMessage;
    private String errorMessage;

    private long discAppId;
    private String submitter;
    private String email;
    private String ipAddress;
    private String subject;
    private String message;
    private List<ReportedThread> reportedThreads = new ArrayList<>();

    private String searchForm;
}
