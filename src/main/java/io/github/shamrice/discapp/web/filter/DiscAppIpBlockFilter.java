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

    @Autowired
    private ApplicationService applicationService;

    @Override
    public void initFilterBean() throws ServletException  {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    /**
     * Filter checks if IP address attempting to access a disc app matches one of the blocked IP prefixes configured
     * for that app. If so, the user is redirected to the permission denied page.
     * @param servletRequest request
     * @param servletResponse response
     * @param filterChain current filter chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //app service is only autowired in some instances when called. if not set, skip check.
        if (applicationService != null && servletRequest instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)servletRequest;
            String url = req.getRequestURL().toString();

            String ipAddress = req.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = req.getRemoteAddr();
            }

            if (url.contains("indices") || url.contains("Indices")) {
                try {
                    String appIdStr = url.substring(url.lastIndexOf("/") + 1).replace(".html", "");

                    long appId = Long.parseLong(appIdStr);
                    List<ApplicationIpBlock> ipBlocks = applicationService.getBlockedIpPrefixes(appId);
                    if (ipBlocks != null) {
                        for (ApplicationIpBlock ipBlock : ipBlocks) {
                            if (ipAddress.contains(ipBlock.getIpAddressPrefix())) {

                                log.warn("Ip address: " + ipAddress + " is blocked from accessing appId: " + appId
                                        + " : matching: " + ipBlock.getIpAddressPrefix()
                                        + " :: redirecting to permission denied page.");
                                HttpServletResponse response = (HttpServletResponse) servletResponse;
                                response.sendRedirect("/error/permissionDenied");
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error checking disc app ip block for ip address: " + ipAddress + " for url: " + url
                            + " :: error: " + ex.getMessage(), ex);
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
