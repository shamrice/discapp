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

    //header/footer config
    private String header;
    private String footer;

    //label config
    private String authorHeader;
    private String dateHeader;
    private String emailHeader;
    private String subjectHeader;
    private String messageHeader;

    //buttons config
    private String shareButton;
    private String editButton;
    private String returnButton;
    private String previewButton;
    private String postButton;
    private String nextPageButton;
    private String replyButton;



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

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getAuthorHeader() {
        return authorHeader;
    }

    public void setAuthorHeader(String authorHeader) {
        this.authorHeader = authorHeader;
    }

    public String getDateHeader() {
        return dateHeader;
    }

    public void setDateHeader(String dateHeader) {
        this.dateHeader = dateHeader;
    }

    public String getEmailHeader() {
        return emailHeader;
    }

    public void setEmailHeader(String emailHeader) {
        this.emailHeader = emailHeader;
    }

    public String getSubjectHeader() {
        return subjectHeader;
    }

    public void setSubjectHeader(String subjectHeader) {
        this.subjectHeader = subjectHeader;
    }

    public String getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(String messageHeader) {
        this.messageHeader = messageHeader;
    }

    public String getShareButton() {
        return shareButton;
    }

    public void setShareButton(String shareButton) {
        this.shareButton = shareButton;
    }

    public String getEditButton() {
        return editButton;
    }

    public void setEditButton(String editButton) {
        this.editButton = editButton;
    }

    public String getReturnButton() {
        return returnButton;
    }

    public void setReturnButton(String returnButton) {
        this.returnButton = returnButton;
    }

    public String getPreviewButton() {
        return previewButton;
    }

    public void setPreviewButton(String previewButton) {
        this.previewButton = previewButton;
    }

    public String getPostButton() {
        return postButton;
    }

    public void setPostButton(String postButton) {
        this.postButton = postButton;
    }

    public String getNextPageButton() {
        return nextPageButton;
    }

    public void setNextPageButton(String nextPageButton) {
        this.nextPageButton = nextPageButton;
    }

    public String getReplyButton() {
        return replyButton;
    }

    public void setReplyButton(String replyButton) {
        this.replyButton = replyButton;
    }
}
