package io.github.shamrice.discapp.web.controller.report;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationReportCode;
import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.application.ApplicationReportCodeService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.enums.AdminReportFrequency;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceWidgetViewModel;
import io.github.shamrice.discapp.web.model.report.AdminReportFrequencyModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class EmailReportController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationReportCodeService applicationReportCodeService;

    @Autowired
    private ConfigurationService configurationService;

    @PostMapping("change-nediscapp-reports-frequency.cgi")
    public ModelAndView postChangeAdminReportFrequency(AdminReportFrequencyModel adminReportFrequencyModel,
                                                       Model model,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        try {
            //submit changes.
            if (adminReportFrequencyModel.getChangeReportFrequency() != null && !adminReportFrequencyModel.getChangeReportFrequency().isEmpty()) {
                Application app = applicationService.get(adminReportFrequencyModel.getAppId());
                if (app != null && applicationReportCodeService.verifyAdminReportCode(
                        app.getId(), adminReportFrequencyModel.getEmailAddress(),
                        adminReportFrequencyModel.getAuthCode())) {

                    log.info("Admin auth code verified successfully. Updating configuration settings.");

                    String reportFrequency = adminReportFrequencyModel.getChangeReportFrequency();
                    if (reportFrequency != null && !reportFrequency.isEmpty()) {
                        String freqToSave = null;
                        if (AdminReportFrequency.DAILY.name().equalsIgnoreCase(reportFrequency)) {
                            freqToSave = AdminReportFrequency.DAILY.name();
                        } else if (AdminReportFrequency.WEEKLY.name().equalsIgnoreCase(reportFrequency)) {
                            freqToSave = AdminReportFrequency.WEEKLY.name();
                        } else if (AdminReportFrequency.MONTHLY.name().equalsIgnoreCase(reportFrequency)) {
                            freqToSave = AdminReportFrequency.MONTHLY.name();
                        } else if (AdminReportFrequency.NEVER.name().equalsIgnoreCase(reportFrequency)) {
                            freqToSave = AdminReportFrequency.NEVER.name();
                        }

                        if (freqToSave != null) {
                            if(!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.MAILING_LIST_ADMIN_REPORT_FREQUENCY, freqToSave)) {
                                log.error("Failed to save admin report frequency: " + adminReportFrequencyModel.toString());
                            }
                        }
                    }
                    adminReportFrequencyModel.setInfoMessage("Report settings updated successfully.");
                } else {
                    log.info("Failed to verify auth code for admin report frequency. Not updating config settings.");
                    adminReportFrequencyModel.setInfoMessage("Unable to save report settings. Please try again.");
                }
            } else {
                log.warn("No model was passed to update admin report frequency. Nothing to do.");
            }
        } catch (Exception ex) {
            log.error("Error saving admin report frequency settings for " + adminReportFrequencyModel.toString(), ex);
            adminReportFrequencyModel.setInfoMessage("Unable to save report settings. Please try again.");
        }

        return new ModelAndView("report/change-nediscapp-reports-frequency", "adminReportFrequencyModel", adminReportFrequencyModel);
    }

    private boolean saveUpdatedConfiguration(long appId, ConfigurationProperty property, String value) {

        if (value == null) {
            log.warn("Attempted to save null configuration value for appId: " + appId + " : config property: " + property.getPropName());
            return false;
        }

        Configuration configToUpdate = configurationService.getConfiguration(appId, property.getPropName());

        if (configToUpdate == null) {
            log.info("Creating new configuration prop: " + property.getPropName() + " for appId: " + appId);
            configToUpdate = new Configuration();
            configToUpdate.setName(property.getPropName());
            configToUpdate.setApplicationId(appId);
        }

        configToUpdate.setValue(value);

        if (!configurationService.saveConfiguration(property, configToUpdate)) {
            log.warn("Failed to update configuration " + property.getPropName() + " of appId: " + appId);
            return false;
        } else {
            log.info("Updated " + property.getPropName() + " for appId: " + appId + " to " + value);
        }

        return true;
    }
}
