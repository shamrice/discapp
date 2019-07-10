package io.github.shamrice.discapp.web.model;

public class NewThreadViewModel {

    private String appId;
    private String submitter;
    private String ipAddress;
    private String subject;
    private String body;
    private String submitNewThread;
    private String returnToApp;

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
}
