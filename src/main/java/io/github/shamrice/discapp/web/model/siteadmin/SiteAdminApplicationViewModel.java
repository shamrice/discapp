package io.github.shamrice.discapp.web.model.siteadmin;

import io.github.shamrice.discapp.data.model.Application;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminApplicationViewModel {
    private String infoMessage;
    private String errorMessage;
    private List<Application> applicationList;
}
