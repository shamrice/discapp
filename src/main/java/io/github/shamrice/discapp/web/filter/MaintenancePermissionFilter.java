package io.github.shamrice.discapp.web.filter;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.EditorPermission;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.permission.UserPermission;
import io.github.shamrice.discapp.web.controller.DiscAppMaintenanceController;
import io.github.shamrice.discapp.web.util.AccountHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@Slf4j
@Component
public class MaintenancePermissionFilter extends GenericFilterBean {

    private static final String THREADS_EDIT_PAGE = "disc-edit.cgi";
    private static final String THREAD_EDIT_PAGE = "edit-thread.cgi";
    private static final String APP_ID_QUERY_STRING_KEY = "?id=";
    private static final String PERMISSION_DENIED_URL = DiscAppMaintenanceController.CONTROLLER_URL_DIRECTORY + "permission-denied";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private AccountHelper accountHelper;

    @Override
    public void initFilterBean() throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //app service is only auto wired in some instances when called. if not set, skip check.
        if (applicationService != null && servletRequest instanceof HttpServletRequest) {

            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse resp = (HttpServletResponse) servletResponse;
            String url = req.getRequestURL().toString();

            if (url.contains(DiscAppMaintenanceController.CONTROLLER_URL_DIRECTORY)
                    && !url.contains(PERMISSION_DENIED_URL)) {

                String email = accountHelper.getLoggedInEmail();
                DiscAppUser discAppUser = discAppUserDetailsService.getByEmail(email);

                if (discAppUser != null ) {

                    try {
                        //remove any extra query params except first id param
                        String queryString = req.getQueryString();
                        if (queryString.contains("&")) {
                            queryString = queryString.substring(0, queryString.indexOf("&"));
                        }
                        String appIdStr = queryString.substring(queryString.lastIndexOf(APP_ID_QUERY_STRING_KEY) + APP_ID_QUERY_STRING_KEY.length());
                        long appId = Long.parseLong(appIdStr);

                        if (!applicationService.isOwnerOfApp(appId, email)) {
                            log.warn("User: " + email + " is not the owner of appid: " + appId + " :: checking if is editor");

                            if (url.contains(THREAD_EDIT_PAGE) || url.contains(THREADS_EDIT_PAGE)) {
                                boolean isEditorOfApp = false;
                                EditorPermission editorPermission = applicationService.getEditorActivePermission(appId, discAppUser.getId());

                                if (editorPermission != null) {
                                    log.info("User: " + email + " is an editor of appId: " + appId + " with perm: " + editorPermission.getUserPermissions());
                                    //set editor permissions if permissions are not set to none.
                                    isEditorOfApp = !editorPermission.getUserPermissions().contains(UserPermission.NONE);
                                }

                                if (!isEditorOfApp) {
                                    log.info("User: " + email + " is not an editor or has " + UserPermission.NONE
                                            + "permission set for appId: " + appId
                                            + " :: redirecting to permission denied");
                                    resp.sendRedirect(PERMISSION_DENIED_URL + APP_ID_QUERY_STRING_KEY + appId);
                                    return;
                                }
                            } else {
                                log.info("User: " + email + " is not the owner of appId: " + appId
                                        + " :: redirecting to permission denied");
                                resp.sendRedirect(PERMISSION_DENIED_URL + APP_ID_QUERY_STRING_KEY + appId);
                                return;
                            }
                        }

                    } catch (Exception ex) {
                        log.error("Error checking for url: " + url + " queryString= " + req.getQueryString()
                                + " :: error: " + ex.getMessage(), ex);
                    }

                    log.info("User: " + email + " is owner of app. Allowing pass to maintenance page. :: Url: " + req.getRequestURL().toString());
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }


}
