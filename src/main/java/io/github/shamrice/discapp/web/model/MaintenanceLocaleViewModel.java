package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MaintenanceLocaleViewModel {

    private long applicationId;
    private String infoMessage;

    //date and time config
    private List<String> timezones;
    private String selectedTimezone;
    private String dateFormat;

    public boolean isCurrentTimezone(String timezone) {
        return this.selectedTimezone.equalsIgnoreCase(timezone);
    }

}
