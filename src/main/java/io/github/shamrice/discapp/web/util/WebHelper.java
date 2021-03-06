package io.github.shamrice.discapp.web.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class WebHelper {

    public String getBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String scheme = request.getScheme() + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + serverName + serverPort + contextPath;
    }
}
