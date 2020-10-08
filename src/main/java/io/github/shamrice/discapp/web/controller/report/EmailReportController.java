package io.github.shamrice.discapp.web.controller.report;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.application.ApplicationReportCodeService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.enums.AdminReportFrequency;
import io.github.shamrice.discapp.web.model.report.AdminReportFrequencyModel;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    private WebHelper webHelper;

    @PostMapping("change-nediscapp-reports-frequency.cgi")
    public ModelAndView postChangeAdminReportFrequency(AdminReportFrequencyModel adminReportFrequencyModel,
                                                       Model model,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        try {
            //submit changes.
            String baseUrl = webHelper.getBaseUrl(request);

            if (adminReportFrequencyModel != null
                    && adminReportFrequencyModel.getAppId() != null
                    && adminReportFrequencyModel.getAuthCode() != null
                    && adminReportFrequencyModel.getEmailAddress() != null
                    && adminReportFrequencyModel.getChangeReportFrequency() != null
                    && !adminReportFrequencyModel.getChangeReportFrequency().isEmpty()) {

                Application app = applicationService.get(adminReportFrequencyModel.getAppId());

                if (app != null) {

                    adminReportFrequencyModel.setAppName(app.getName());
                    adminReportFrequencyModel.setBaseUrl(baseUrl);

                    if (applicationReportCodeService.verifyAdminReportCode(
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
                                if (!configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.MAILING_LIST_ADMIN_REPORT_FREQUENCY, freqToSave)) {
                                    log.error("Failed to save admin report frequency: " + adminReportFrequencyModel.toString());
                                    adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
                                }
                            }
                        }
                        adminReportFrequencyModel.setInfoMessage("Report settings updated successfully.");
                    } else {
                        log.info("Failed to verify auth code for admin report frequency. Not updating config settings.");
                        adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
                    }
                } else {
                    log.warn("Admin report frequency did not find appId: " + adminReportFrequencyModel.getAppId());
                    //set base values if model is missing.
                    adminReportFrequencyModel = new AdminReportFrequencyModel();
                    adminReportFrequencyModel.setBaseUrl(baseUrl);
                    adminReportFrequencyModel.setAppName("Help Forum");
                    adminReportFrequencyModel.setAppId(1L);
                    adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
                }
            } else {
                log.warn("Model passed to update admin report frequency missing fields. Nothing to do.");
                if (adminReportFrequencyModel != null) {
                    log.warn("Failed model passed to admin report frequency controller: " + adminReportFrequencyModel.toString());
                }
                //set base values if model is missing.
                adminReportFrequencyModel = new AdminReportFrequencyModel();
                adminReportFrequencyModel.setBaseUrl(baseUrl);
                adminReportFrequencyModel.setAppName("Help Forum");
                adminReportFrequencyModel.setAppId(1L);
                adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
            }
        } catch (Exception ex) {
            log.error("Error saving admin report frequency settings for " + adminReportFrequencyModel.toString(), ex);
            adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
        }

        return new ModelAndView("report/change-nediscapp-reports-frequency", "adminReportFrequencyModel", adminReportFrequencyModel);
    }
}
