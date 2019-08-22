package io.github.shamrice.discapp.web.model;

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

    //todo break these into sub views inside master view.
    private boolean onEditMessage;
    private boolean onEditModifyMessage; //todo: maybe move to enum of view name instead of a sea of boolean flags...
    private String editArticle;
    private String editArticleChangeMessage;
    private String editArticleCancelEdit;
    private Long editArticleId;
    private String pagemark;
    private String editArticleSubmitter;
    private String editArticleEmail;
    private String editArticleSubject;
    private String editArticleCreateDt;
    private String editArticleModDt;
    private String editArticleIpAddress;
    private String editArticleUserAgent;
    private String editArticleMessage;
    private String editArticleReplyThreadsHtml;

}
