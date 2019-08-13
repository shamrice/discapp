package io.github.shamrice.discapp.web.model;

import java.util.List;

public class MaintenanceThreadViewModel {

    private long applicationId;

    private String tab;
    private String infoMessage;
    private long numberOfMessages;
    private List<String> editThreadTreeHtml;

    private String deleteArticles;
    private String deleteArticlesAndReplies;
    private String reportAbuse;
    private String nextPage;
    private long nextPageStart;


    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public long getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(long numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public List<String> getEditThreadTreeHtml() {
        return editThreadTreeHtml;
    }

    public void setEditThreadTreeHtml(List<String> editThreadTreeHtml) {
        this.editThreadTreeHtml = editThreadTreeHtml;
    }

    public String getDeleteArticles() {
        return deleteArticles;
    }

    public void setDeleteArticles(String deleteArticles) {
        this.deleteArticles = deleteArticles;
    }

    public String getDeleteArticlesAndReplies() {
        return deleteArticlesAndReplies;
    }

    public void setDeleteArticlesAndReplies(String deleteArticlesAndReplies) {
        this.deleteArticlesAndReplies = deleteArticlesAndReplies;
    }

    public String getReportAbuse() {
        return reportAbuse;
    }

    public void setReportAbuse(String reportAbuse) {
        this.reportAbuse = reportAbuse;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public long getNextPageStart() {
        return nextPageStart;
    }

    public void setNextPageStart(long nextPageStart) {
        this.nextPageStart = nextPageStart;
    }
}
