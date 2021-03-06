package io.github.shamrice.discapp.web.model.maintenance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MaintenanceThreadViewModel {

    //aka 'GOD' view...

    private long applicationId;

    private boolean hasUnapprovedMessages;
    private String approve;

    private String tab;
    private String infoMessage;
    private long numberOfMessages;
    private List<String> editThreadTreeHtml;

    private String deleteArticles;
    private String deleteArticlesAndReplies;
    private String makeThisTopLevelArticle;
    private String reportAbuse;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private Integer nextPage;
    private Integer previousPage;
    private Integer currentPage;
    private String nextPageSubmit;
    private String previousPageSubmit;

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

    private String newThreadEmail;
    private String newThreadSubmitter;
    private boolean newThreadShowEmail;
    private String newThreadSubject;
    private boolean newThreadAdminPost;
    private String newThreadMessage;
    private String postArticle;

    //todo break these into sub views inside master view.
    private boolean onEditMessage;
    private boolean onEditModifyMessage; //todo: maybe move to enum of view name instead of a sea of boolean flags...
    private String editArticle;
    private String editArticleChangeMessage;
    private String editArticleCancelEdit;
    private Long editArticleId;
    private Long editArticleParentId;
    private String pagemark;
    private String editArticleSubmitter;
    private String editArticleEmail;
    private String editArticleSubject;
    private boolean editArticleAdminPost;
    private String editArticleCreateDt;
    private String editArticleModDt;
    private String editArticleIpAddress;
    private String editArticleUserAgent;
    private String editArticleMessage;
    private String editArticleReplyThreadsHtml;
    private String editArticleCurrentUsername;
    private String editArticleUserEmail;
    private Long editArticleUserId;

    private boolean isSystemAccount;

    public Integer getNextPage() {
        return currentPage == null ? 1 : currentPage + 1;
    }

    public Integer getPreviousPage() {
        return currentPage == null ? 0 : currentPage - 1;
    }

    public boolean isApprovedSearchChecked(String value) {
        if (value == null) {
            return false;
        }

        //if value isn't set yet, say true for approved.
        if (approvedSearch.isEmpty() && "approved".equalsIgnoreCase(value)) {
            return true;
        }

        return value.equalsIgnoreCase(approvedSearch);
    }

}
