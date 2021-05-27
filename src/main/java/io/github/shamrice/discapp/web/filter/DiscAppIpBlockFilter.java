package io.github.shamrice.discapp.web.filter;

import io.github.shamrice.discapp.data.model.ApplicationIpBlock;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.define.url.AppCustomCssUrl;
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
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.AuthenticationUrl.AUTH_INDICES_URL;

@Component
@Slf4j
public class DiscAppIpBlockFilter extends GenericFilterBean {

    private static final String PERMISSION_DENIED_URL = "/error/permissionDenied";
    private static final String APP_INDICES_URL = "indices";
    private static final String SEARCH_URL = "search";
    private static final String SEARCH_APP_ID_QUERY_PARAM = "disc";

    @Autowired
    private ApplicationService applicationService;

    @Override
    public void initFilterBean() throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //app service is only auto wired in some instances when called. if not set, skip check.
        if (applicationService != null && servletRequest instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            String url = req.getRequestURL().toString();

            String ipAddress = req.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = req.getRemoteAddr();
            }

            //handle site wide IP blocks first.
            if (!url.contains(PERMISSION_DENIED_URL)) {
                handleIpPrefixBlockedForApp(servletResponse, ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ipAddress);
            }

            //todo : fix so filter works correctly on /indices/search?disc=appid urls.
            if (url.toLowerCase().contains(APP_INDICES_URL) && !url.toLowerCase().contains(AUTH_INDICES_URL) && !url.toLowerCase().contains(AppCustomCssUrl.CUSTOM_CSS_URL_PREFIX)) {

                try {
                    String appIdStr;
                    if (url.toLowerCase().contains(SEARCH_URL)) {
                        MultiValueMap<String, String> params = UriComponentsBuilder
                                .fromUriString(req.getRequestURL().toString() + "?" + req.getQueryString())
                                .build()
                                .getQueryParams();

                        appIdStr = params.getFirst(SEARCH_APP_ID_QUERY_PARAM);
                    } else {
                        appIdStr = url.substring(url.lastIndexOf("/") + 1).replace(".html", "");
                    }

                    if (appIdStr != null && !appIdStr.isEmpty() && appIdStr.chars().allMatch(Character::isDigit)) {
                        long appId = Long.parseLong(appIdStr);

                        handleIpPrefixBlockedForApp(servletResponse, appId, ipAddress);
                        handleAbuseIpBlockedForApp(servletResponse, appId, ipAddress);
                    }

                } catch (Exception ex) {
                    log.error("Error checking disc app ip block for ip address: " + ipAddress + " for url: " + url
                            + " :: error: " + ex.getMessage(), ex);
                }
            }
        }
        if (!servletResponse.isCommitted()) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * Filter checks if ip address accessing application has a record in the reported abuse table for that disc app. If
     * user has been reported previously. They will not be able to access the disc app.
     * @param servletResponse resposne
     * @param appId appId to check for
     * @param ipAddress source ip address making the call.
     * @throws IOException
     */
    private void handleAbuseIpBlockedForApp(ServletResponse servletResponse, long appId, String ipAddress) throws IOException {

        List<String> reportedAbuseIps = applicationService.getReportedAbuseIpAddressesForApplication(appId);
        if (reportedAbuseIps != null) {
            for (String abuseIp : reportedAbuseIps) {
                if (ipAddress.equalsIgnoreCase(abuseIp)) {

                    log.warn("Ip address: " + ipAddress + " is blocked from accessing appId: " + appId
                            + " : matching reported abuse ip: " + abuseIp
                            + " :: redirecting to permission denied page.");
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    if (!response.isCommitted()) {
                        response.sendRedirect(PERMISSION_DENIED_URL);
                        return;
                    } else {
                        log.info("Response already committed. Skipping IP block redirect to permission denied page.");
                    }
                }
            }
        }
    }

    /**
     * Filter checks if IP address attempting to access a disc app matches one of the blocked IP prefixes configured
     * for that app. If so, the user is redirected to the permission denied page.
     *
     * @param servletResponse response
     * @param appId appId being attempted
     * @param ipAddress source Ip address of request.
     */
    private void handleIpPrefixBlockedForApp(ServletResponse servletResponse, long appId, String ipAddress) throws IOException {

        if (ipAddress == null || ipAddress.isBlank()) {
            log.info("Cannot check if IP is blocked on appId: " + appId + ". IP is null or empty.");
            return;
        }

        List<ApplicationIpBlock> ipBlocks = applicationService.getBlockedIpPrefixes(appId);
        if (ipBlocks != null) {
            for (ApplicationIpBlock ipBlock : ipBlocks) {
                if (ipAddress.startsWith(ipBlock.getIpAddressPrefix())) {
                    log.warn("Ip address: " + ipAddress + " is blocked from accessing appId: " + appId
                            + " : matching prefix: " + ipBlock.getIpAddressPrefix()
                            + " :: redirecting to permission denied page.");
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    if (!response.isCommitted()) {
                        response.sendRedirect(PERMISSION_DENIED_URL);
                        return;
                    } else {
                        log.info("Response already committed. Skipping IP block redirect to permission denied page.");
                    }
                }
            }
        }
    }
}
