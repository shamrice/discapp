package io.github.shamrice.discapp.web.configuration;

import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.MAINTENANCE_PAGE;

@Slf4j
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    public CustomLoginSuccessHandler(String defaultTargUrl) {
        setDefaultTargetUrl(defaultTargUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        //redirect system admin accounts to their related disc maintenance page.
        DiscAppUserPrincipal userPrincipal = (DiscAppUserPrincipal)authentication.getPrincipal();
        if (userPrincipal != null) {
            log.debug("Login user id: " + userPrincipal.getEmail());

            discAppUserDetailsService.setLastLoginDateToNow(userPrincipal.getId());

            if (!userPrincipal.isUserAccount()) {
                log.info("User: " + userPrincipal.getEmail() + " :: is System admin account. Redirecting to related admin page.");

                String redirect = MAINTENANCE_PAGE + "?id=" + userPrincipal.getEmail();
                getRedirectStrategy().sendRedirect(request, response, redirect);
                return;
            }
        }

        //todo : set authentication make age. <-- I no longer know what that means.
       super.onAuthenticationSuccess(request, response, authentication);
    }
}
