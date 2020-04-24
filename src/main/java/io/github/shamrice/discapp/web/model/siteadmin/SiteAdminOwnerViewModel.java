package io.github.shamrice.discapp.web.model.siteadmin;

import io.github.shamrice.discapp.data.model.Owner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminOwnerViewModel {

    private String errorMessage;
    private String userType;
    private Owner owner;
}
