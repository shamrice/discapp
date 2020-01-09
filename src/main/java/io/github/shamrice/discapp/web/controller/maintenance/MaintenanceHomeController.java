package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.MAINTENANCE_PAGE;

@Controller
@Slf4j
public class MaintenanceHomeController extends MaintenanceController {


    @GetMapping(CONTROLLER_URL_DIRECTORY + "permission-denied")
    public ModelAndView getPermissionDeniedView(@RequestParam(name = "id") long appId,
                                                HttpServletResponse response,
                                                Model model) {
        model.addAttribute(APP_NAME, "");
        model.addAttribute(APP_ID, appId);
        String username = accountHelper.getLoggedInEmail();
        model.addAttribute(USERNAME, username);

        return new ModelAndView("admin/permissionDenied");
    }

    @GetMapping(MAINTENANCE_PAGE)
    public ModelAndView getDiscMaintenanceView(@RequestParam(name = "id") long appId,
                                               Model model,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            return new ModelAndView("admin/disc-maint");

        } catch (Exception ex) {
            log.error("Error getting maintenance page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-toolbar.cgi")
    public ModelAndView getDiscToolbarView(@RequestParam(name = "id") long appId,
                                           Model model,
                                           HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            return new ModelAndView("admin/disc-toolbar");

        } catch (Exception ex) {
            log.error("Error getting maintenance toolbar page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-info.cgi")
    public ModelAndView getDiscInfoView(@RequestParam(name = "id") long appId,
                                        Model model,
                                        HttpServletResponse response,
                                        HttpServletRequest request) {
        String baseUrl = webHelper.getBaseUrl(request);
        model.addAttribute(APP_URL, baseUrl + "/indices/" + appId);
        //todo : indices string should be from static final property

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            model.addAttribute(IS_ADMIN, true);

        } catch (Exception ex) {
            log.error("Failed to display landing page for maintenance for appid: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("admin/disc-info");
    }

}
