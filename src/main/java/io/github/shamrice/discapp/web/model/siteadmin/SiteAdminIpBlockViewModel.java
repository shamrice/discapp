package io.github.shamrice.discapp.web.model.siteadmin;

import io.github.shamrice.discapp.data.model.ApplicationIpBlock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminIpBlockViewModel {

    private String infoMessage;
    private String errorMessage;
    private String newIpBlockPrefix;
    private String newIpBlockReason;
    private List<ApplicationIpBlock> ipBlockList;
}
