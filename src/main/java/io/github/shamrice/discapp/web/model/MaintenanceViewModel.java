package io.github.shamrice.discapp.web.model;

import java.util.Date;

public class MaintenanceViewModel {

    private String redirect;
    private String infoMessage;

    //application config
    private Long applicationId;
    private String applicationName;
    private Date applicationCreateDt;
    private Date applicationModDt;

    //prologue / epilogue config
    private String prologueText;
    private String epilogueText;
    private Date prologueModDt;
    private Date epilogueModDt;

    //style sheet config
    private String styleSheetUrl;

    // thread config
    private String threadSortOrder;
    private boolean expandThreadsOnIndex;
    private boolean previewFirstMessageOnIndex;
    private boolean highlightNewMessages;
    private String threadBreak;
    private String entryBreak;
    private int threadDepth;


    public boolean isSelectedThreadDepth(int dropDownValue) {
        return threadDepth == dropDownValue;
    }

    public boolean isThreadSortOrderCreation() {
        return threadSortOrder.equalsIgnoreCase("creation");
    }


    public boolean isThreadSortOrderActivity() {
        return threadSortOrder.equalsIgnoreCase("activity");
    }


    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public Date getApplicationCreateDt() {
        return applicationCreateDt;
    }

    public void setApplicationCreateDt(Date applicationCreateDt) {
        this.applicationCreateDt = applicationCreateDt;
    }

    public Date getApplicationModDt() {
        return applicationModDt;
    }

    public void setApplicationModDt(Date applicationModDt) {
        this.applicationModDt = applicationModDt;
    }

    public String getPrologueText() {
        return prologueText;
    }

    public void setPrologueText(String prologueText) {
        this.prologueText = prologueText;
    }

    public String getEpilogueText() {
        return epilogueText;
    }

    public void setEpilogueText(String epilogueText) {
        this.epilogueText = epilogueText;
    }

    public Date getPrologueModDt() {
        return prologueModDt;
    }

    public void setPrologueModDt(Date prologueModDt) {
        this.prologueModDt = prologueModDt;
    }

    public Date getEpilogueModDt() {
        return epilogueModDt;
    }

    public void setEpilogueModDt(Date epilogueModDt) {
        this.epilogueModDt = epilogueModDt;
    }

    public String getStyleSheetUrl() {
        return styleSheetUrl;
    }

    public void setStyleSheetUrl(String styleSheetUrl) {
        this.styleSheetUrl = styleSheetUrl;
    }

    public boolean isExpandThreadsOnIndex() {
        return expandThreadsOnIndex;
    }

    public void setExpandThreadsOnIndex(boolean expandThreadsOnIndex) {
        this.expandThreadsOnIndex = expandThreadsOnIndex;
    }

    public boolean isPreviewFirstMessageOnIndex() {
        return previewFirstMessageOnIndex;
    }

    public void setPreviewFirstMessageOnIndex(boolean previewFirstMessageOnIndex) {
        this.previewFirstMessageOnIndex = previewFirstMessageOnIndex;
    }

    public boolean isHighlightNewMessages() {
        return highlightNewMessages;
    }

    public void setHighlightNewMessages(boolean highlightNewMessages) {
        this.highlightNewMessages = highlightNewMessages;
    }

    public String getThreadBreak() {
        return threadBreak;
    }

    public void setThreadBreak(String threadBreak) {
        this.threadBreak = threadBreak;
    }

    public String getEntryBreak() {
        return entryBreak;
    }

    public void setEntryBreak(String entryBreak) {
        this.entryBreak = entryBreak;
    }

    public int getThreadDepth() {
        return threadDepth;
    }

    public void setThreadDepth(int threadDepth) {
        this.threadDepth = threadDepth;
    }


    public void setThreadSortOrder(String threadSortOrder) {
        this.threadSortOrder = threadSortOrder;
    }

    public String getThreadSortOrder() {
        return this.threadSortOrder;
    }
}
