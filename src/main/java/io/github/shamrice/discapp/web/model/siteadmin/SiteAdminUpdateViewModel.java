package io.github.shamrice.discapp.web.model.siteadmin;

import io.github.shamrice.discapp.data.model.SiteUpdateLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminUpdateViewModel {
    private String infoMessage;
    private String errorMessage;

    private String newUpdateSubject;
    private String newUpdateText;
    private String submit;

    private List<SiteUpdateLog> siteUpdateLogList = new ArrayList<>();

    private String editUpdateSubject;
    private String editUpdateText;
    private long editUpdateId;
}
