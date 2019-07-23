package io.github.shamrice.discapp.web.model;

public class NewThreadViewModel {

    private String appId;
    private String submitter;
    private String email;
    private String ipAddress;
    private String subject;
    private String body;
    private String parentId;
    private String submitNewThread;
    private String returnToApp;
    private String previewArticle;
    private boolean showEmail;
    private String htmlBody;
    private String parentThreadSubmitter;
    private String parentThreadSubject;
    private String parentThreadBody;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReturnToApp() {
        return returnToApp;
    }

    public void setReturnToApp(String returnToApp) {
        this.returnToApp = returnToApp;
    }

    public String getSubmitNewThread() {
        return submitNewThread;
    }

    public void setSubmitNewThread(String submitNewThread) {
        this.submitNewThread = submitNewThread;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPreviewArticle() {
        return previewArticle;
    }

    public void setPreviewArticle(String previewArticle) {
        this.previewArticle = previewArticle;
    }

    public boolean isShowEmail() {
        return showEmail;
    }

    public void setShowEmail(boolean showEmail) {
        this.showEmail = showEmail;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getParentThreadSubmitter() {
        return parentThreadSubmitter;
    }

    public void setParentThreadSubmitter(String parentThreadSubmitter) {
        this.parentThreadSubmitter = parentThreadSubmitter;
    }

    public String getParentThreadSubject() {
        return parentThreadSubject;
    }

    public void setParentThreadSubject(String parentThreadSubject) {
        this.parentThreadSubject = parentThreadSubject;
    }

    public String getParentThreadBody() {
        return parentThreadBody;
    }

    public void setParentThreadBody(String parentThreadBody) {
        this.parentThreadBody = parentThreadBody;
    }
}
