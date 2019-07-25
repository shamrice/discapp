package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.repository.DiscappUserRepository;
import io.github.shamrice.discapp.service.application.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiscAppMaintenanceController {

    private static Logger logger = LoggerFactory.getLogger(DiscAppMaintenanceController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscappUserRepository discappUserRepository;

    @GetMapping("/admin/disc-maint.cgi")
    public String getMaintenanceView(@RequestParam( name = "id") String appId, Model model) {

        try {
            Long id = Long.parseLong(appId);
            Application app = applicationService.get(id);

            if (app != null) {
                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());
            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return "admin/disc-maint";
    }
}
