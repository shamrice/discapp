package io.github.shamrice.discapp.web.model.maintenance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class MaintenanceViewModel {

    private String infoMessage;

    //application config
    private String applicationInfoMessage;
    private Long applicationId;
    private String applicationName;
    private Date applicationCreateDt;
    private Date applicationModDt;

    //prologue / epilogue config
    private String prologueEpilogueInfoMessage;
    private String prologueText;
    private String epilogueText;
    private Date prologueModDt;
    private Date epilogueModDt;

    //style sheet config
    private String styleSheetInfoMessage;
    private String styleSheetSelected;
    private String styleSheetUrl;
    private String styleSheetCustomText;

    // thread config
    private String threadInfoMessage;
    private String threadSortOrder;
    private boolean expandThreadsOnIndex;
    private boolean previewFirstMessageOnIndex;
    private boolean highlightNewMessages;
    private String threadBreak;
    private String entryBreak;
    private int maxThreadCountPerPage;
    private int threadDepth;
    private int previewTopLevelLength;
    private int previewReplyLength;

    //header/footer config
    private String headerFooterInfoMessage;
    private String header;
    private String footer;

    //label config
    private String labelInfoMessage;
    private String authorHeader;
    private String dateHeader;
    private String emailHeader;
    private String subjectHeader;
    private String messageHeader;

    //buttons config
    private String buttonInfoMessage;
    private String shareButton;
    private String editButton;
    private String returnButton;
    private String previewButton;
    private String postButton;
    private String previousPageButton;
    private String nextPageButton;
    private String replyButton;

    //favicon config
    private String faviconInfoMessage;
    private String favicon;
    private String faviconFileName;
    private String faviconStyleSelected;
    private String reuseFaviconFile;

    //window state config
    private String windowState;
    private String submittedFromDiv;

    //hold permissions config
    private String holdInfoMessage;
    private boolean displayPostHoldMessage;
    private boolean displayAfterPostHoldMessage;
    private String displayPostHoldMessageText;
    private String displayAfterPostHoldMessageText;

    public boolean isSelectedMaxThreadCountPerPage(int dropDownValue) {
        return maxThreadCountPerPage == dropDownValue;
    }

    public boolean isSelectedThreadDepth(int dropDownValue) {
        return threadDepth == dropDownValue;
    }

    public boolean isSelectedPreviewTopLevelLength(int dropDownValue) {
        return previewTopLevelLength == dropDownValue;
    }

    public boolean isSelectedPreviewReplyLength(int dropDownValue) {
        return previewReplyLength == dropDownValue;
    }

    public boolean isThreadSortOrderCreation() {
        return threadSortOrder.equalsIgnoreCase("creation");
    }

    public boolean isThreadSortOrderActivity() {
        return threadSortOrder.equalsIgnoreCase("activity");
    }

    public boolean isSelectedCssStyle(String dropDownValue) {
        return styleSheetSelected.equalsIgnoreCase(dropDownValue);
    }

    public boolean isSelectedFaviconStyle(String dropDownValue) {
        return faviconStyleSelected.equalsIgnoreCase(dropDownValue);
    }

}
