package io.github.shamrice.discapp.web.define.url;

public class AccountUrl {

    public static final String CONTROLLER_DIRECTORY_URL = "/account/";

    public static final String ACCOUNT_ADD_APPLICATION = CONTROLLER_DIRECTORY_URL + "add/application";
    public static final String ACCOUNT_MODIFY_APPLICATION = CONTROLLER_DIRECTORY_URL + "modify/application";
    public static final String ACCOUNT_MODIFY_OWNER = CONTROLLER_DIRECTORY_URL + "modify/owner";

    public static final String ACCOUNT_MODIFY_ACCOUNT = CONTROLLER_DIRECTORY_URL + "modify/account";

    public static final String ACCOUNT_MODIFY_PASSWORD = CONTROLLER_DIRECTORY_URL + "modify/password";

    public static final String ACCOUNT_MODIFY = CONTROLLER_DIRECTORY_URL + "modify";

    public static final String ACCOUNT_MODIFY_READ_STATUS = ACCOUNT_MODIFY + "/read/{status}";
    public static final String ACCOUNT_MODIFY_READ_RESET = ACCOUNT_MODIFY + "/read/reset";

    public static final String ACCOUNT_CREATE = CONTROLLER_DIRECTORY_URL + "create";
    public static final String ACCOUNT_CREATE_ACCOUNT_SUCCESS = CONTROLLER_DIRECTORY_URL + "createAccountSuccess";
    public static final String ACCOUNT_USER_REGISTRATION = CONTROLLER_DIRECTORY_URL + "registration";
    public static final String ACCOUNT_DELETE = CONTROLLER_DIRECTORY_URL + "delete";
    public static final String ACCOUNT_DELETE_STATUS = ACCOUNT_DELETE + "/status";
    public static final String ACCOUNT_PASSWORD = CONTROLLER_DIRECTORY_URL + "password";
    public static final String ACCOUNT_PASSWORD_RESET = CONTROLLER_DIRECTORY_URL + "password/{resetKey}";

    public static final String ACCOUNT_APPLICATION = CONTROLLER_DIRECTORY_URL + "application";
}
