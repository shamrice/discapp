package io.github.shamrice.discapp.web.model;

import java.util.List;

public class MaintenanceThreadViewModel {

    //aka 'GOD' view...

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

    private String[] selectThreadCheckbox;

    private String findMessages;
    private String authorSearch = "";
    private String emailSearch = "";
    private String subjectSearch = "";
    private String ipSearch = "";
    private String messageSearch = "";
    private String approvedSearch = "";
    private boolean searchSubmitted;
    private String searchAgain;

    private String newThreadSubject;
    private String newThreadMessage;
    private String postArticle;


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


    public String[] getSelectThreadCheckbox() {
        return selectThreadCheckbox;
    }

    public void setSelectThreadCheckbox(String[] selectThreadCheckbox) {
        this.selectThreadCheckbox = selectThreadCheckbox;
    }

    public String getFindMessages() {
        return findMessages;
    }

    public void setFindMessages(String findMessages) {
        this.findMessages = findMessages;
    }

    public String getAuthorSearch() {
        return authorSearch;
    }

    public void setAuthorSearch(String authorSearch) {
        this.authorSearch = authorSearch;
    }

    public String getEmailSearch() {
        return emailSearch;
    }

    public void setEmailSearch(String emailSearch) {
        this.emailSearch = emailSearch;
    }

    public String getSubjectSearch() {
        return subjectSearch;
    }

    public void setSubjectSearch(String subjectSearch) {
        this.subjectSearch = subjectSearch;
    }

    public String getIpSearch() {
        return ipSearch;
    }

    public void setIpSearch(String ipSearch) {
        this.ipSearch = ipSearch;
    }

    public String getMessageSearch() {
        return messageSearch;
    }

    public void setMessageSearch(String messageSearch) {
        this.messageSearch = messageSearch;
    }

    public String getApprovedSearch() {
        return approvedSearch;
    }

    public void setApprovedSearch(String approvedSearch) {
        this.approvedSearch = approvedSearch;
    }

    public boolean isSearchSubmitted() {
        return searchSubmitted;
    }

    public void setSearchSubmitted(boolean searchSubmitted) {
        this.searchSubmitted = searchSubmitted;
    }

    public String getSearchAgain() {
        return searchAgain;
    }

    public void setSearchAgain(String searchAgain) {
        this.searchAgain = searchAgain;
    }

    public String getNewThreadSubject() {
        return newThreadSubject;
    }

    public void setNewThreadSubject(String newThreadSubject) {
        this.newThreadSubject = newThreadSubject;
    }

    public String getNewThreadMessage() {
        return newThreadMessage;
    }

    public void setNewThreadMessage(String newThreadMessage) {
        this.newThreadMessage = newThreadMessage;
    }

    public String getPostArticle() {
        return postArticle;
    }

    public void setPostArticle(String postArticle) {
        this.postArticle = postArticle;
    }
}
