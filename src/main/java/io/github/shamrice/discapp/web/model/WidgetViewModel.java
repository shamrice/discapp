package io.github.shamrice.discapp.web.model;

import java.util.List;

public class WidgetViewModel {

    private long applicationId;
    private String styleSheetUrl;
    private String faviconUrl;
    private List<String> threadsHtml;

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public String getStyleSheetUrl() {
        return styleSheetUrl;
    }

    public void setStyleSheetUrl(String styleSheetUrl) {
        this.styleSheetUrl = styleSheetUrl;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public List<String> getThreadsHtml() {
        return threadsHtml;
    }

    public void setThreadsHtml(List<String> threadsHtml) {
        this.threadsHtml = threadsHtml;
    }
}
