package io.github.shamrice.discapp.web.model;

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

    public String getWidgetWidthUnit() {
        return widgetWidthUnit;
    }

    public void setWidgetWidthUnit(String widgetWidthUnit) {
        this.widgetWidthUnit = widgetWidthUnit;
    }

    public String getWidgetHeightUnit() {
        return widgetHeightUnit;
    }

    public void setWidgetHeightUnit(String widgetHeightUnit) {
        this.widgetHeightUnit = widgetHeightUnit;
    }

    public String getWidgetWidth() {
        return widgetWidth;
    }

    public void setWidgetWidth(String widgetWidth) {
        this.widgetWidth = widgetWidth;
    }

    public String getWidgetHeight() {
        return widgetHeight;
    }

    public void setWidgetHeight(String widgetHeight) {
        this.widgetHeight = widgetHeight;
    }

    public boolean isShowAuthor() {
        return showAuthor;
    }

    public void setShowAuthor(boolean showAuthor) {
        this.showAuthor = showAuthor;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }

    public boolean isShowStyleSheet() {
        return showStyleSheet;
    }

    public void setShowStyleSheet(boolean showStyleSheet) {
        this.showStyleSheet = showStyleSheet;
    }

    public String getSubmitChanges() {
        return submitChanges;
    }

    public void setSubmitChanges(String submitChanges) {
        this.submitChanges = submitChanges;
    }

    public String getCodeHtml() {
        return codeHtml;
    }

    public void setCodeHtml(String codeHtml) {
        this.codeHtml = codeHtml;
    }
}
