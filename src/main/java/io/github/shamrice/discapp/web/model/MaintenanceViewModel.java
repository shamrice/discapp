package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
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
    private String previousPageButton;
    private String nextPageButton;
    private String replyButton;

    //favicon config
    private String favicon;

    public boolean isSelectedThreadDepth(int dropDownValue) {
        return threadDepth == dropDownValue;
    }

    public boolean isThreadSortOrderCreation() {
        return threadSortOrder.equalsIgnoreCase("creation");
    }

    public boolean isThreadSortOrderActivity() {
        return threadSortOrder.equalsIgnoreCase("activity");
    }

}
