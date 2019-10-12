package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceSecurityViewModel {

    private long applicationId;
    private String infoMessage;
    private String errorMessage;

    private String ownerEmail;
    private String changeOwnerEmail;

    private String permissionMessage;

    private String unregisteredPermissions;
    private String registeredPermissions;
    private String changeDefaultAccess;

    private String editUrl;

    private String changeUserAccess;
    private String deleteUsers;
    private String searchUsersForm;

    private boolean showIp;
    private boolean blockBadWords;
    private boolean blockSearch;
    private String changeSecurity;

    //todo : use a list or an array or something...
    private String blockIp1;
    private String blockIp2;
    private String blockIp3;
    private String blockIp4;
    private String blockIp5;
    private String blockIp6;
    private String changeIPs;

    private String blockHtml; //values = allow, subject, forbid.
    private String changeHTMLPerms;

    public boolean isBlockHtml(String radioValue) {
        if (blockHtml == null || radioValue == null) return false;
        return blockHtml.equalsIgnoreCase(radioValue.toLowerCase());
    }
}
