package io.github.shamrice.discapp.web.define.url;

public class SiteAdminUrl {

    public static final String CONTROLLER_URL_DIRECTORY = "/site_admin/";

    public static final String SUBSCRIBERS_URL = CONTROLLER_URL_DIRECTORY + "subscribers";
    public static final String SUBSCRIBER_ENABLED = CONTROLLER_URL_DIRECTORY + "subscriber/enabled";

    public static final String ACCOUNTS_URL = CONTROLLER_URL_DIRECTORY + "accounts";
    public static final String ACCOUNT_SHOW_EMAIL = CONTROLLER_URL_DIRECTORY + "account/showEmail";
    public static final String ACCOUNT_ENABLED = CONTROLLER_URL_DIRECTORY + "account/enabled";
    public static final String ACCOUNT_IS_ADMIN = CONTROLLER_URL_DIRECTORY + "account/isAdmin";
    public static final String ACCOUNT_IS_USER_ACCOUNT = CONTROLLER_URL_DIRECTORY + "account/isUserAccount";

    public static final String OWNER_URL = CONTROLLER_URL_DIRECTORY + "owner";

    public static final String APPLICATIONS_URL = CONTROLLER_URL_DIRECTORY + "applications";
    public static final String APPLICATION_ENABLED = CONTROLLER_URL_DIRECTORY + "application/enabled";
    public static final String APPLICATION_DELETED = CONTROLLER_URL_DIRECTORY + "application/deleted";
    public static final String APPLICATION_IS_SEARCHABLE = CONTROLLER_URL_DIRECTORY + "application/isSearchable";

    public static final String IMPORTS_URL = CONTROLLER_URL_DIRECTORY + "imports";
    public static final String IMPORT_DELETE = CONTROLLER_URL_DIRECTORY + "import/delete";
    public static final String IMPORT_DOWNLOAD = CONTROLLER_URL_DIRECTORY + "import/download";

    public static final String THREADS_URL = CONTROLLER_URL_DIRECTORY + "threads";
    public static final String THREAD_RESTORE = CONTROLLER_URL_DIRECTORY + "thread/restore";

    public static final String UPDATE_URL = CONTROLLER_URL_DIRECTORY + "update";
    public static final String UPDATE_MANAGE = CONTROLLER_URL_DIRECTORY + "update/manage";
    public static final String UPDATE_ENABLED = CONTROLLER_URL_DIRECTORY + "update/enabled";
    public static final String UPDATE_EDIT = CONTROLLER_URL_DIRECTORY + "update/edit";
}
