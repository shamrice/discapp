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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("change-nediscapp-reports-frequency.cgi")
    public String getChangeAdminReportFrequency() {
        //redirect user to account application section to do it manually.
        return "redirect:/account/application";
    }

    @PostMapping("change-nediscapp-reports-frequency.cgi")
    public ModelAndView postChangeAdminReportFrequency(AdminReportFrequencyModel adminReportFrequencyModel,
                                                       Model model,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {

        boolean updateSuccess = true;
        String baseUrl = webHelper.getBaseUrl(request);

        try {
            //verify model
            if (adminReportFrequencyModel == null
                    || adminReportFrequencyModel.getAppIds() == null
                    || adminReportFrequencyModel.getAuthCode() == null
                    || adminReportFrequencyModel.getEmailAddress() == null
                    || adminReportFrequencyModel.getChangeReportFrequency() == null
                    || adminReportFrequencyModel.getChangeReportFrequency().isEmpty()) {

                log.warn("Fields missing in model passed to controller.");
                return failedUpdateModelAndView(adminReportFrequencyModel, baseUrl);
            }

            //process changes.
            adminReportFrequencyModel.setBaseUrl(baseUrl);

            String freqToSave = null;
            String reportFrequency = adminReportFrequencyModel.getChangeReportFrequency();
            if (reportFrequency != null && !reportFrequency.isEmpty()) {
                if (AdminReportFrequency.DAILY.name().equalsIgnoreCase(reportFrequency)) {
                    freqToSave = AdminReportFrequency.DAILY.name();
                } else if (AdminReportFrequency.WEEKLY.name().equalsIgnoreCase(reportFrequency)) {
                    freqToSave = AdminReportFrequency.WEEKLY.name();
                } else if (AdminReportFrequency.MONTHLY.name().equalsIgnoreCase(reportFrequency)) {
                    freqToSave = AdminReportFrequency.MONTHLY.name();
                } else if (AdminReportFrequency.NEVER.name().equalsIgnoreCase(reportFrequency)) {
                    freqToSave = AdminReportFrequency.NEVER.name();
                }
            }

            if (freqToSave == null) {
                log.error("Frequency: " + reportFrequency + " is not a valid option. Not saving report settings.");
                return failedUpdateModelAndView(adminReportFrequencyModel, baseUrl);
            }

            String[] appIdsStr = adminReportFrequencyModel.getAppIds().split(",");
            List<Long> appIds = new ArrayList<>();
            for (String appIdStr : appIdsStr) {
                try {
                    long appId = Long.parseLong(appIdStr);
                    if (appId > 0) {
                        appIds.add(appId);
                    }
                } catch (Exception ex) {
                    log.error("Failed to parse appId from string array: " + appIdStr + " :: skipping. :: " + ex.getMessage());
                }
            }

            if (appIds.isEmpty()) {
                log.warn("No valid app ids parsed from list: " + adminReportFrequencyModel.getAppIds() + ". Nothing to do.");
                return failedUpdateModelAndView(adminReportFrequencyModel, baseUrl);
            }

            //verify auth code & email address against all appIds passed.
            for (long appId : appIds) {
                if (!applicationReportCodeService.verifyAdminReportCode(appId,
                        adminReportFrequencyModel.getEmailAddress(),
                        adminReportFrequencyModel.getAuthCode())) {

                    log.warn("Failed to verify authorization for updating admin report frequency for appId: " + appId);
                    return failedUpdateModelAndView(adminReportFrequencyModel, baseUrl);
                }
            }

            log.info("Admin auth codes verified successfully. Updating configuration settings.");

            //if verified, update each one.
            for (long appId : appIds) {

                Application app = applicationService.get(appId);

                if (app != null) {
                    if (!configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.MAILING_LIST_ADMIN_REPORT_FREQUENCY, freqToSave)) {
                        log.error("Failed to save admin report frequency for appId: " + app.getId() + " :: Frequency: " + freqToSave);
                        updateSuccess = false;
                    } else {
                        adminReportFrequencyModel.setInfoMessage("Report settings updated successfully.");
                    }

                } else {
                    log.error("Admin report frequency did not find appId: " + appId);
                    updateSuccess = false;
                }
            }
        } catch (Exception ex) {
            log.error("Error saving admin report frequency settings.", ex);
            updateSuccess = false;
        }

        if (!updateSuccess) {
            return failedUpdateModelAndView(adminReportFrequencyModel, baseUrl);
        }

        return new ModelAndView("report/change-nediscapp-reports-frequency", "adminReportFrequencyModel", adminReportFrequencyModel);
    }

    private ModelAndView failedUpdateModelAndView(AdminReportFrequencyModel adminReportFrequencyModel, String baseUrl) {

        if (adminReportFrequencyModel == null) {
            adminReportFrequencyModel = new AdminReportFrequencyModel();
        }
        adminReportFrequencyModel.setErrorMessage("Unable to save report settings. Please try again.");
        adminReportFrequencyModel.setBaseUrl(baseUrl);

        return new ModelAndView("report/change-nediscapp-reports-frequency", "adminReportFrequencyModel", adminReportFrequencyModel);
    }
}
