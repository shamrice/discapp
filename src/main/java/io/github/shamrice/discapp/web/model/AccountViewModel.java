package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class AccountViewModel {

    private String username;
    private String password;
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

    private String ownerFirstName;
    private String ownerLastName;
    private String ownerPhone;
    private String ownerEmail;
    private Long applicationId;
    private String applicationName;

    private String redirect;

    public boolean isOwner() {
        return this.ownerId != null;
    }

}
