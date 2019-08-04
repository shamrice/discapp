package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.web.util.AccountHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AccountHelper accountHelper;

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(required = false) String redirect,
                              @RequestParam(required = false) String appName,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap) {

        //get redirect from referer header if not in query param.
        if (redirect == null || redirect.isEmpty()) {

            redirect = "/account/modify"; //default

            if (request != null) {
                //attempt from cookie first...
                boolean foundInCookie = false;
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if (cookie.getName().equalsIgnoreCase("redirect_url")) {
                            redirect = cookie.getValue();
                            foundInCookie = true;
                            break;
                        }
                    }
                }

                //follow up attempt by referrer and then default.
                if (!foundInCookie) {
                    String headerRdirect = request.getHeader("Referer");
                    if (headerRdirect != null && !headerRdirect.isEmpty()) {
                        redirect = headerRdirect;
                    }
                }
            }
        }

        //if name missing set to redirect url.
        if (appName == null || appName.isEmpty()) {
            appName = redirect;
        }

        //if account is already logged in, bring them to the log out page
        if (accountHelper.isLoggedIn()) {
            return logout(redirect, appName, request, response, modelMap);
        }

        response.addCookie(new Cookie("redirect_url", redirect));
        modelMap.addAttribute("redirectUrl", redirect);
        modelMap.addAttribute("appName", appName);

        return new ModelAndView("auth/login", modelMap);
    }

    @GetMapping("/logout")
    public ModelAndView logout(@RequestParam(required = false) String redirect,
                         @RequestParam(required = false) String appName,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         ModelMap modelMap) {

        //get redirect from referer header if not in query param.
        if (redirect == null || redirect.isEmpty()) {
            redirect = request.getHeader("Referer");
        }

        //if name missing, set it to redirect url
        if (appName == null || appName.isEmpty()) {
            appName = redirect;
        }

        response.addCookie(new Cookie("redirect_url", redirect));
        modelMap.addAttribute("redirectUrl", redirect);
        modelMap.addAttribute("appName", appName);

        return new ModelAndView("auth/logout", modelMap);
    }

}
