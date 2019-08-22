package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MaintenanceWidgetViewModel {

    private long applicationId;
    private String infoMessage;
    private String widgetWidthUnit;
    private String widgetHeightUnit;
    private String widgetWidth;
    private String widgetHeight;
    private boolean showAuthor;
    private boolean showDate;
    private boolean showStyleSheet;
    private String submitChanges;
    private String codeHtml;

    public boolean isHeightUnit(String unit) {
        if (unit == null || unit.isEmpty()) {
            return false;
        }
        return unit.equalsIgnoreCase(this.widgetHeightUnit);
    }

    public boolean isWidthUnit(String unit) {
        if (unit == null || unit.isEmpty()) {
            return false;
        }
        return unit.equalsIgnoreCase(this.widgetWidthUnit);
    }
}
