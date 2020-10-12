package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.enums.RssBehavior;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceRssViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.RSS_MAINTENANCE_PAGE;
import static io.github.shamrice.discapp.web.define.url.RssUrl.DISCUSSION_RSS_URL;

@Controller
@Slf4j
public class RssMaintenanceController extends MaintenanceController {

    @GetMapping(RSS_MAINTENANCE_PAGE)
    public ModelAndView getRssMaintenanceView(@RequestParam(name = "id") long appId,
                                              MaintenanceRssViewModel maintenanceRssViewModel,
                                              Model model,
                                              HttpServletRequest request) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            maintenanceRssViewModel.setApplicationId(app.getId());
            maintenanceRssViewModel.setRssBehavior(configurationService.getStringValue(app.getId(), ConfigurationProperty.RSS_BEHAVIOR, RssBehavior.ALL.name()));

            String baseUrl = webHelper.getBaseUrl(request);

            maintenanceRssViewModel.setRssFeedUrl(baseUrl + DISCUSSION_RSS_URL + "?id=" + appId);

        } catch (Exception ex) {
            log.error("Error getting RSS Maintenance page.", ex);
            maintenanceRssViewModel.setErrorMessage("Failed to get RSS settings.");
        }

        return new ModelAndView("admin/disc-rss-maint", "maintenanceRssViewModel", maintenanceRssViewModel);
    }

    @PostMapping(RSS_MAINTENANCE_PAGE)
    public ModelAndView postRssMaintenanceView(@RequestParam(name = "id") long appId,
                                              MaintenanceRssViewModel maintenanceRssViewModel,
                                              Model model,
                                              HttpServletRequest request) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceRssViewModel.setApplicationId(app.getId());
            String baseUrl = webHelper.getBaseUrl(request);
            maintenanceRssViewModel.setRssFeedUrl(baseUrl + DISCUSSION_RSS_URL + "?id=" + appId);

            String rssBehavior;
            if (RssBehavior.FIRST.name().equalsIgnoreCase(maintenanceRssViewModel.getRssBehavior())) {
                rssBehavior = RssBehavior.FIRST.name();
            } else if (RssBehavior.PREVIEW.name().equalsIgnoreCase(maintenanceRssViewModel.getRssBehavior())) {
                rssBehavior = RssBehavior.PREVIEW.name();
            } else {
                rssBehavior = RssBehavior.ALL.name();
            }

            if (!configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.RSS_BEHAVIOR, rssBehavior)) {
                maintenanceRssViewModel.setErrorMessage("Failed to update RSS settings.");
            } else {
                maintenanceRssViewModel.setInfoMessage("RSS behavior updated.");
            }

        } catch (Exception ex) {
            log.error("Error saving RSS Maintenance page settings.", ex);
            maintenanceRssViewModel.setErrorMessage("Failed to save RSS settings. Please try again.");
        }

        return new ModelAndView("admin/disc-rss-maint", "maintenanceRssViewModel", maintenanceRssViewModel);
    }
}
