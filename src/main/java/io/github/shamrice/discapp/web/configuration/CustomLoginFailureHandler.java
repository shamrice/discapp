package io.github.shamrice.discapp.web.configuration;

import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.web.define.url.AuthenticationUrl;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String USERNAME_REQUEST_PARAMETER = "username";

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private WebHelper webHelper;

    public CustomLoginFailureHandler() {
        super.setDefaultFailureUrl(AuthenticationUrl.LOGIN + AuthenticationUrl.LOGIN_ERROR_PARAMETER);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

        String username = httpServletRequest.getParameter(USERNAME_REQUEST_PARAMETER);
        if (username != null && username.trim().length() > 0) {
            discAppUserDetailsService.incrementPasswordLastFailCount(username, webHelper.getBaseUrl(httpServletRequest));
        }

        if (e.getCause() != null && e.getCause() instanceof LockedException) {
            logger.error("Account locked: " + username + " :: setting login error message to locked.");
            httpServletResponse.sendRedirect(AuthenticationUrl.LOGIN + AuthenticationUrl.LOGIN_LOCKED_PARAMETER);
        } else{
            super.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
        }

    }
}