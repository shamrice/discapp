package io.github.shamrice.discapp.web.model.maintenance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceRssViewModel {

    private long applicationId;
    private String infoMessage;
    private String errorMessage;

    private String rssFeedUrl;
    private String rssBehavior;
    private String changeRss;

    public boolean isRssBehaviorChecked(String behaviorValue) {
        if (rssBehavior == null || behaviorValue == null) return false;
        return rssBehavior.equalsIgnoreCase(behaviorValue);
    }

}
