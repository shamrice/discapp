package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceSecurityViewModel {

    private long applicationId;
    private String infoMessage;
    private String errorMessage;

    private String ownerEmail;
    private String changeOwnerEmail;
    private String ownerEmailMessage;


    private String unregisteredPermissions;
    private String registeredPermissions;
    private String changeDefaultAccess;
    private String permissionMessage;

    private String editUrl;
    private String changeUserAccess;
    private String deleteUsers;
    private String searchUsersForm;
    private String editorPermissionMessage;

    private boolean showIp;
    private boolean blockBadWords;
    private boolean blockSearch;
    private String changeSecurity;
    private String securityMessage;

    //todo : use a list or an array or something...
    private String[] blockIpList = new String[6];
    private String blockIp1;
    private String blockIp2;
    private String blockIp3;
    private String blockIp4;
    private String blockIp5;
    private String blockIp6;
    private String changeIPs;
    private String ipMessage;

    private String blockHtml; //values = allow, subject, forbid.
    private String changeHTMLPerms;
    private String htmlMessage;

    public boolean isBlockHtml(String radioValue) {
        if (blockHtml == null || radioValue == null) return false;
        return blockHtml.equalsIgnoreCase(radioValue.toLowerCase());
    }

    public boolean isUnregisteredUsersPermissionChecked(String permissionValue) {
        if (unregisteredPermissions == null || permissionValue == null) return false;
        return unregisteredPermissions.equalsIgnoreCase(permissionValue.toLowerCase());
    }

    public boolean isRegisteredUsersPermissionChecked(String permissionValue) {
        if (registeredPermissions == null || permissionValue == null) return false;
        return registeredPermissions.equalsIgnoreCase(permissionValue.toLowerCase());
    }
}
