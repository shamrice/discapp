package io.github.shamrice.discapp.web.define.url;

public class AbuseUrl {

    public static final String CONTROLLER_DIRECTORY_URL = "/abuse/";

    public static final String ABUSE_DELETE = CONTROLLER_DIRECTORY_URL + "delete";
    public static final String ABUSE_VIEW = CONTROLLER_DIRECTORY_URL + "abuse.cgi";
    public static final String ABUSE_SEARCH = CONTROLLER_DIRECTORY_URL + "abuse-search.cgi";
    public static final String ABUSE_SEARCH_VIEW = CONTROLLER_DIRECTORY_URL + "abuse-view.cgi";

    public static final String ARTICLE_ID_QUERY_PARAM = "articleId";
    public static final String ID_QUERY_PARAM = "id";
    public static final String ABUSE_ID_QUERY_PARAM = "abuseId";
    public static final String DISC_APP_ID_QUERY_PARAM = "discId";
    public static final String SUBMITTER_QUERY_PARAM = "submitter";
    public static final String EMAIL_QUERY_PARAM = "email";
    public static final String IP_QUERY_PARAM = "ip";
    public static final String SUBJECT_QUERY_PARAM = "subject";
    public static final String BODY_QUERY_PARAM = "body";
}
