package io.github.shamrice.discapp.web.model;

import java.util.Date;

public class MaintenanceViewModel {

    private String redirect;
    private String infoMessage;
    private Long applicationId;
    private String applicationName;
    private Date applicationCreateDt;
    private Date applicationModDt;
    private String prologueText;
    private String epilogueText;
    private Date prologueModDt;
    private Date epilogueModDt;
    private String styleSheetUrl;



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
}
