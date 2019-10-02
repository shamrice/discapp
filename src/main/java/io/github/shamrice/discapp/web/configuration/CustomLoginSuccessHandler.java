package io.github.shamrice.discapp.web.configuration;

import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomLoginSuccessHandler(String defaultTargUrl) {
        setDefaultTargetUrl(defaultTargUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        //for redirect on system admin accounts to their related disc maintenance page.
        DiscAppUserPrincipal userPrincipal = (DiscAppUserPrincipal)authentication.getPrincipal();
        if (userPrincipal != null) {
            log.warn("Login by user: " + userPrincipal.toString());
            if (!userPrincipal.isUserAccount()) {
                log.info("Account: " + userPrincipal.toString() + " :: is System admin account. Redirecting to related admin page.");
                //todo : probably should pull that string from somewhere instead of just hard coded...
                String redirect = "/admin/disc-maint.cgi?id=" + userPrincipal.getUsername();
                getRedirectStrategy().sendRedirect(request, response, redirect);
                return;
            }
        }
        //todo : set authentication make age.

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
