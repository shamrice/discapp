package io.github.shamrice.discapp.web.filter;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.UserPermission;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.web.define.url.MaintenanceUrl;
import io.github.shamrice.discapp.web.util.AccountHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class MaintenancePermissionFilter extends GenericFilterBean {

    private static final String APP_ID_QUERY_STRING_KEY = "id";

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

            //root accounts are not filtered.
            if (!accountHelper.isRootAdminAccount()) {

                HttpServletRequest req = (HttpServletRequest) servletRequest;
                HttpServletResponse resp = (HttpServletResponse) servletResponse;
                String url = req.getRequestURL().toString();

                if (url.contains(MaintenanceUrl.CONTROLLER_DIRECTORY_URL)
                        && !url.contains(MaintenanceUrl.PERMISSION_DENIED_URL)) {

                    String email = accountHelper.getLoggedInEmail();
                    DiscAppUser discAppUser = discAppUserDetailsService.getByEmail(email);

                    if (discAppUser != null) {

                        try {
                            MultiValueMap<String, String> params = UriComponentsBuilder
                                    .fromUriString(req.getRequestURL().toString() + "?" + req.getQueryString())
                                    .build()
                                    .getQueryParams();
                            String appIdStr = params.getFirst(APP_ID_QUERY_STRING_KEY);

                            if (appIdStr != null) {
                                long appId = Long.parseLong(appIdStr);

                                if (!applicationService.isUserAccountOwnerOfApp(appId, email)) {
                                    log.warn("User: " + email + " is not the owner of appid: " + appId + " :: checking if is editor");

                                    if (url.contains(MaintenanceUrl.THREAD_EDIT_PAGE) || url.contains(MaintenanceUrl.THREADS_EDIT_PAGE)) {
                                        boolean isEditorOfApp = false;
                                        UserPermission userPermission = applicationService.getUserActivePermission(appId, discAppUser.getId());

                                        if (userPermission != null) {
                                            log.info("User: " + email + " is an editor of appId: " + appId + " with perm: " + userPermission.getUserPermissions());
                                            //set editor permissions if permissions are not set to none.
                                            isEditorOfApp = userPermission.getUserPermissions().contains(io.github.shamrice.discapp.service.application.permission.UserPermission.EDIT);
                                        }

                                        if (!isEditorOfApp) {
                                            log.info("User: " + email + " is not an editor or does not have '" + io.github.shamrice.discapp.service.application.permission.UserPermission.EDIT
                                                    + "' permission set for appId: " + appId
                                                    + " :: redirecting to permission denied");
                                            resp.sendRedirect(MaintenanceUrl.PERMISSION_DENIED_URL + "?" + APP_ID_QUERY_STRING_KEY + "=" + appId);
                                            return;
                                        }
                                    } else {
                                        log.info("User: " + email + " is not the owner of appId: " + appId
                                                + " :: redirecting to permission denied");
                                        resp.sendRedirect(MaintenanceUrl.PERMISSION_DENIED_URL + "?" + APP_ID_QUERY_STRING_KEY + "=" + appId);
                                        return;
                                    }
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
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
