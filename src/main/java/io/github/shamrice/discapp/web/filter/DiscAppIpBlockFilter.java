package io.github.shamrice.discapp.web.filter;

import io.github.shamrice.discapp.data.model.ApplicationIpBlock;
import io.github.shamrice.discapp.service.application.ApplicationService;
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
import java.util.List;

@Component
@Slf4j
public class DiscAppIpBlockFilter extends GenericFilterBean {

    private static final String PERMISSION_DENIED_URL = "/error/permissionDenied";
    private static final String APP_INDICES_URL = "indices";

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

            if (url.toLowerCase().contains(APP_INDICES_URL)) {

                String ipAddress = req.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null || ipAddress.isEmpty()) {
                    ipAddress = req.getRemoteAddr();
                }

                try {
                    String appIdStr = url.substring(url.lastIndexOf("/") + 1).replace(".html", "");

                    long appId = Long.parseLong(appIdStr);

                    handleIpPrefixBlockedForApp(servletResponse, appId, ipAddress);
                    handleAbuseIpBlockedForApp(servletResponse, appId, ipAddress);

                } catch (Exception ex) {
                    log.error("Error checking disc app ip block for ip address: " + ipAddress + " for url: " + url
                            + " :: error: " + ex.getMessage(), ex);
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
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

        List<ApplicationIpBlock> ipBlocks = applicationService.getBlockedIpPrefixes(appId);
        if (ipBlocks != null) {
            for (ApplicationIpBlock ipBlock : ipBlocks) {
                if (ipAddress.contains(ipBlock.getIpAddressPrefix())) {

                    log.warn("Ip address: " + ipAddress + " is blocked from accessing appId: " + appId
                            + " : matching: " + ipBlock.getIpAddressPrefix()
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
