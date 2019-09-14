package io.github.shamrice.discapp.web.model;

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
    }

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

    private List<AccountApplication> accountApplications;

    private String redirect;

    public boolean isOwner() {
        return this.ownerId != null;
    }

}
