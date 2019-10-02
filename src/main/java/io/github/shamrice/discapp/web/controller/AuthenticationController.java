package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.web.util.AccountHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
public class AuthenticationController {

    @Autowired
    private AccountHelper accountHelper;

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(required = false) String redirect,
                              @RequestParam(required = false) String appName,
                              @RequestParam(required = false) String relogin,
                              @RequestParam(required = false) Long appId,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap) {

        //show disc app admin login box if attempting to access maintenance page.
        if (request != null && request.getSession() != null) {
            try {
                DefaultSavedRequest savedReq = (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                if (savedReq != null && savedReq.getRequestURI().contains("disc-maint.cgi")) {
                    String[] idVals = savedReq.getParameterValues("id");
                    if (idVals.length > 0)
                        modelMap.addAttribute("appId", idVals[0]);
                }
            } catch (Exception ex) {
                log.error("Error trying to get default saved request from session object. " + ex.getMessage(), ex);
            }
        }

        //if name missing set to redirect url.
        if (appName == null || appName.isEmpty()) {
            appName = redirect;
        }

        //if account is already logged in, bring them to the log out page
        if (accountHelper.isLoggedIn() && (relogin == null || relogin.isEmpty())) {
            return logout(redirect, appName, request, response, modelMap);
        }

        //todo : remove cookie stuff.
        Cookie redirectCookie = new Cookie("redirect_url", redirect);
        redirectCookie.setMaxAge(-1); //only live as long as the browser stays on the window
        response.addCookie(redirectCookie);
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

        Cookie redirectCookie = new Cookie("redirect_url", redirect);
        redirectCookie.setMaxAge(-1); //only live as long as the browser stays on the window.
        response.addCookie(redirectCookie);
        modelMap.addAttribute("redirectUrl", redirect);
        modelMap.addAttribute("appName", appName);

        return new ModelAndView("auth/logout", modelMap);
    }

    /**
     * This method is a landing page for users who need to log into a disc app. It will redirect
     * them back to the disc app that sent them or index 1 after they log in
     * @param appId disc app application id
     * @param modelMap model map. not used.
     * @return redirects user to base page of disc app.
     */
    @GetMapping("/auth/indices")
    public ModelAndView getAuthIndices(@RequestParam(required = false, name = "id") Long appId,
                               ModelMap modelMap) {
        if (appId == null || appId <= 0) {
            appId = 1L;
        }
        return new ModelAndView("redirect:/indices/" + appId);
    }

}
