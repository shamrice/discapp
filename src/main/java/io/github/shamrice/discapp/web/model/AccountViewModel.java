package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.Application;
import lombok.*;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AccountViewModel {

    @RequiredArgsConstructor
    @Getter
    public static class AccountApplication {
        private @NonNull String applicationName;
        private @NonNull Long applicationId;
        private @NonNull String applicationStatus;
        private @NonNull String applicationSearchStatus;

        public boolean isSelectedApplicationStatus(String dropDownValue) {
            return applicationStatus.equalsIgnoreCase(dropDownValue);
        }

        public boolean isSelectedApplicationSearchStatus(String dropDownValue) {
            return applicationSearchStatus.equalsIgnoreCase(dropDownValue);
        }
    }

    private String cancel;

    private String username;
    private String password;
    private String newPassword;
    private String confirmPassword;
    private boolean showEmail;
    private String email;
    private Long ownerId;
    private boolean enabled;
    private boolean isAdmin;
    private Date createDt;
    private Date modDt;
    private String errorMessage;
    private String infoMessage;

    private String reCaptchaResponse;

    private String ownerFirstName;
    private String ownerLastName;
    private String ownerPhone;
    private String ownerEmail;
    private Long applicationId;
    private String applicationName;
    private String applicationStatus;
    private String applicationSearchStatus;

    private Integer maxDiscApps;

    private String applicationAdminPassword;
    private String confirmApplicationAdminPassword;

    private String baseEditorUrl;

    private List<AccountApplication> accountApplications;
    private List<AccountApplication> moderatingApplications;

    private List<Application> userReadThreadApplications;

    private boolean readTrackingEnabled;

    public boolean isBelowAppLimit() {
        if (maxDiscApps == null) {
            return true;
        }

        if (accountApplications == null) {
            return true;
        }

        return accountApplications.size() < maxDiscApps;
    }

    public boolean isOwner() {
        return this.ownerId != null;
    }

}
