package io.github.shamrice.discapp.web.define.url;

public class AppUrl {

    //todo: expand this

    public static final String CONTROLLER_DIRECTORY_URL = "/indices/";
    public static final String CONTROLLER_DIRECTORY_URL_ALTERNATE = "/Indices/";

    public static final String ALTERNATE_APPLICATION_VIEW_URL = CONTROLLER_DIRECTORY_URL_ALTERNATE + "{applicationId}.html";
    public static final String APPLICATION_VIEW_URL = CONTROLLER_DIRECTORY_URL + "{applicationId}";

    public static final String CREATE_THREAD = "/createThread";
    public static final String POST_THREAD = "/postThread";
    public static final String PREVIEW_THREAD = "/previewThread";

    public static final String DISCUSSION_URL = "discussion.cgi";

    public static final String APP_NUMBER_SUFFIX_ALTERNATE = ".html";

    public static final String APP_SEARCH_URL = CONTROLLER_DIRECTORY_URL + "search";
}
