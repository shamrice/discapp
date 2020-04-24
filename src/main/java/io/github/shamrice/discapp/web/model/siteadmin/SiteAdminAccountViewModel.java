package io.github.shamrice.discapp.web.model.siteadmin;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminAccountViewModel {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class User {
        Long id;
        String username;
        String email;
        Boolean showEmail;
        Long ownerId;
        Boolean enabled;
        Boolean isAdmin;
        Boolean isUserAccount;
        String lastLoginDate;
        String createDt;
        String modDt;
    }

    private String infoMessage;
    private String errorMessage;
    private String userType;
    private List<User> userList;
}
