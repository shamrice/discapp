package io.github.shamrice.discapp.web.model;

import java.util.List;

public class MaintenanceLocaleViewModel {

    private long applicationId;
    private String infoMessage;
    private String redirect;

    //date and time config
    private List<String> timezones;
    private String selectedTimezone;
    private String dateFormat;

    public boolean isCurrentTimezone(String timezone) {
        return this.selectedTimezone.equalsIgnoreCase(timezone);
    }

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public List<String> getTimezones() {
        return timezones;
    }

    public void setTimezones(List<String> timezones) {
        this.timezones = timezones;
    }

    public String getSelectedTimezone() {
        return selectedTimezone;
    }

    public void setSelectedTimezone(String selectedTimezone) {
        this.selectedTimezone = selectedTimezone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
