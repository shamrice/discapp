package io.github.shamrice.discapp.web.define.url;

public class AuthenticationUrl {

    public static final String CONTROLLER_DIRECTORY_URL = "/";

    public static final String LOGIN_ERROR_PARAMETER = "?error";
    public static final String LOGIN_LOCKED_PARAMETER = "?locked";

    public static final String LOGIN = CONTROLLER_DIRECTORY_URL + "login";
    public static final String LOGOUT = CONTROLLER_DIRECTORY_URL + "logout";
    public static final String AUTH_INDICES_URL = "/auth/indices";
}
