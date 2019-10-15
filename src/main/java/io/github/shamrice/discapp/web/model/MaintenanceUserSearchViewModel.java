package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceUserSearchViewModel {
    private long applicationId;
    private String infoMessage;
    private String errorMessage;

    private String searchTerm;
    private String addAccounts;
    private String searchUsers;
    private String cancel;

    private List<DiscAppUser> searchResults = new ArrayList<>();
    private List<Long> addAccountId = new ArrayList<>();
}
