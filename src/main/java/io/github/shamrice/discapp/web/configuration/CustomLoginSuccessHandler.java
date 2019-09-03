package io.github.shamrice.discapp.web.configuration;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomLoginSuccessHandler(String defaultTargUrl) {
        setDefaultTargetUrl(defaultTargUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        //attempt destination value set in cookie first.
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("redirect_url")) {
                getRedirectStrategy().sendRedirect(request, response, cookie.getValue());
                return;
            }
        }

        //todo : set authentication make age.

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
