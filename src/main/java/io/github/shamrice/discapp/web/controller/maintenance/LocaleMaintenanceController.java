package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceLocaleViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Controller
@Slf4j
public class LocaleMaintenanceController extends MaintenanceController {

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-locale.cgi")
    public ModelAndView getDiscLocaleView(@RequestParam(name = "id") long appId,
                                          MaintenanceLocaleViewModel maintenanceLocaleViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceLocaleViewModel.setApplicationId(app.getId());

            //time and date config
            String timezone = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");
            maintenanceLocaleViewModel.setSelectedTimezone(timezone);

            String dateFormat = configurationService.getStringValue(appId, ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");
            maintenanceLocaleViewModel.setDateFormat(dateFormat);

            String[] timezoneIds = TimeZone.getAvailableIDs();
            List<String> timezones = Arrays.asList(timezoneIds);
            maintenanceLocaleViewModel.setTimezones(timezones);

        } catch (Exception ex) {
            log.error("Error getting locale admin view: " + ex.getMessage(), ex);
            maintenanceLocaleViewModel.setInfoMessage("An error has occurred. Please try again.");
        }

        return new ModelAndView("admin/disc-locale", "maintenanceLocaleViewModel", maintenanceLocaleViewModel);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/time")
    public ModelAndView postModifyTime(@RequestParam(name = "id") long appId,
                                       @ModelAttribute MaintenanceLocaleViewModel maintenanceLocaleViewModel,
                                       Model model,
                                       HttpServletResponse response) {

        if (maintenanceLocaleViewModel.getDateFormat() == null || maintenanceLocaleViewModel.getDateFormat().trim().isEmpty()) {
            maintenanceLocaleViewModel.setDateFormat("EEE MMM dd, yyyy h:mma");
        }

        Application app = applicationService.get(appId);

        String dateFormat = inputHelper.sanitizeInput(maintenanceLocaleViewModel.getDateFormat());

        boolean timezoneSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.TIMEZONE_LOCATION, maintenanceLocaleViewModel.getSelectedTimezone());
        boolean dateFormatSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.DATE_FORMAT_PATTERN, dateFormat);

        if (timezoneSaved && dateFormatSaved) {
            log.info("Saved date and time settings for appId: " + appId);
            maintenanceLocaleViewModel.setInfoMessage("Successfully saved changes to Date and Time.");
        } else {
            maintenanceLocaleViewModel.setInfoMessage("Failed to save changes to Date and Time.");
        }

        return getDiscLocaleView(appId, maintenanceLocaleViewModel, model, response);
    }

}
