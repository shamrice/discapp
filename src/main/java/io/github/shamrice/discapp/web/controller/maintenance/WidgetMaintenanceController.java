package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.web.define.url.WidgetUrl;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceWidgetViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class WidgetMaintenanceController extends MaintenanceController {

    @PostMapping(CONTROLLER_URL_DIRECTORY + "disc-widget-maint.cgi")
    public ModelAndView postDiscMainWidgetView(@RequestParam(name = "id") long appId,
                                               MaintenanceWidgetViewModel maintenanceWidgetViewModel,
                                               Model model,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //submit changes.
            if (maintenanceWidgetViewModel.getSubmitChanges() != null && !maintenanceWidgetViewModel.getSubmitChanges().isEmpty()) {
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, String.valueOf(maintenanceWidgetViewModel.isShowAuthor()).toLowerCase());
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, String.valueOf(maintenanceWidgetViewModel.isShowDate()).toLowerCase());
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, String.valueOf(maintenanceWidgetViewModel.isShowStyleSheet()).toLowerCase());

                int width = 20;
                try {
                    width = Integer.parseInt(maintenanceWidgetViewModel.getWidgetWidth());
                } catch (NumberFormatException widthEx) {
                    log.warn("Invalid widget width for appId: " + appId
                            + " : value: " + maintenanceWidgetViewModel.getWidgetWidth() + " :: using default: "
                            + width + " :: " + widthEx.getMessage());
                }
                maintenanceWidgetViewModel.setWidgetWidth(String.valueOf(width));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH, String.valueOf(width));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, maintenanceWidgetViewModel.getWidgetWidthUnit());

                int height = 18;
                try {
                    height = Integer.parseInt(maintenanceWidgetViewModel.getWidgetHeight());
                } catch (NumberFormatException heightEx) {
                    log.warn("Invalid widget height for appId: " + appId
                            + " : value: " + maintenanceWidgetViewModel.getWidgetHeight() + " :: using default: "
                            + height + " :: " + heightEx.getMessage());
                }
                maintenanceWidgetViewModel.setWidgetHeight(String.valueOf(height));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, String.valueOf(height));
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, maintenanceWidgetViewModel.getWidgetHeightUnit());
            } else {
                //reset to default values.
                //todo : probably should get these values from somewhere in the config service or something...
                String width = "20";
                String widthUnit = "em";
                String height = "18";
                String heightUnit = "em";
                String showAuthor = "true";
                String showDate = "false";
                String useStyleSheet = "true";

                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, showAuthor);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, showDate);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, useStyleSheet);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH, width);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, widthUnit);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, height);
                saveUpdatedConfiguration(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, heightUnit);

                maintenanceWidgetViewModel.setWidgetWidth(width);
                maintenanceWidgetViewModel.setWidgetWidthUnit(widthUnit);
                maintenanceWidgetViewModel.setWidgetHeight(height);
                maintenanceWidgetViewModel.setWidgetHeightUnit(heightUnit);
                maintenanceWidgetViewModel.setShowAuthor(Boolean.parseBoolean(showAuthor));
                maintenanceWidgetViewModel.setShowDate(Boolean.parseBoolean(showDate));
                maintenanceWidgetViewModel.setShowStyleSheet(Boolean.parseBoolean(useStyleSheet));
            }

        } catch (Exception ex) {
            log.error("Error saving widget settings for appId: " + appId + " :: " + ex.getMessage(), ex);
            maintenanceWidgetViewModel.setInfoMessage("Unable to save widget settings. Please try again.");
        }

        return getDiscMaintWidgetView(appId, maintenanceWidgetViewModel, model, request, response);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-widget-maint.cgi")
    public ModelAndView getDiscMaintWidgetView(@RequestParam(name = "id") long appId,
                                               MaintenanceWidgetViewModel maintenanceWidgetViewModel,
                                               Model model,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceWidgetViewModel.setApplicationId(app.getId());

            boolean showAuthor = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_SHOW_AUTHOR, true);
            boolean showDate = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_SHOW_DATE, false);
            boolean useStyleSheet = configurationService.getBooleanValue(app.getId(), ConfigurationProperty.WIDGET_USE_STYLE_SHEET, true);
            String width = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_WIDTH, "20");
            String widthUnit = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_WIDTH_UNIT, "em");
            String height = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_HEIGHT, "18");
            String heightUnit = configurationService.getStringValue(app.getId(), ConfigurationProperty.WIDGET_HEIGHT_UNIT, "em");

            maintenanceWidgetViewModel.setShowAuthor(showAuthor);
            maintenanceWidgetViewModel.setShowDate(showDate);
            maintenanceWidgetViewModel.setShowStyleSheet(useStyleSheet);
            maintenanceWidgetViewModel.setWidgetWidth(width);
            maintenanceWidgetViewModel.setWidgetWidthUnit(widthUnit);
            maintenanceWidgetViewModel.setWidgetHeight(height);
            maintenanceWidgetViewModel.setWidgetHeightUnit(heightUnit);

            String heightUnitForCode = maintenanceWidgetViewModel.getWidgetHeightUnit();
            String widthUnitForCode = maintenanceWidgetViewModel.getWidgetWidthUnit();

            if (heightUnitForCode.equalsIgnoreCase("percent")) {
                heightUnitForCode = "%";
            }

            if (widthUnitForCode.equalsIgnoreCase("percent")) {
                widthUnitForCode = "%";
            }

            String baseUrl = webHelper.getBaseUrl(request);

            maintenanceWidgetViewModel.setCodeHtml(
                    getWidgetHtml(
                            Integer.parseInt(maintenanceWidgetViewModel.getWidgetWidth()),
                            widthUnitForCode,
                            Integer.parseInt(maintenanceWidgetViewModel.getWidgetHeight()),
                            heightUnitForCode,
                            app.getId(),
                            baseUrl
                    )
            );

        } catch (Exception ex) {
            log.error("Error getting widget maintenance page for : " + appId + " :: " + ex.getMessage(), ex);
            maintenanceWidgetViewModel.setInfoMessage("An unexpected error has occurred. Please try again.");
        }

        return new ModelAndView("admin/disc-widget-maint", "maintenanceWidgetViewModel", maintenanceWidgetViewModel);
    }


    private String getWidgetHtml(Integer width, String widthUnit, Integer height, String heightUnit, long appId, String baseUrl) {
        return "<div style=\"width:" + width + widthUnit + "; height:" + height + heightUnit + "; margin:4%; padding:1ex; \n" +
                "    border:1px solid black; float:right;\">\n" +
                "\n" +
                " Here's your widget! Put your header here.\n" +
                "\n" +
                "<iframe src=\"" + baseUrl + WidgetUrl.CONTROLLER_URL_DIRECTORY + "disc-widget.cgi?disc=" + appId + "\" \n" +
                "        width=\"99%\" height=\"80%\" frameborder=\"no\" scrolling=\"no\">\n" +
                "</iframe>\n" +
                "\n" +
                " Put your footer here.\n" +
                "\n" +
                "</div>\n";
    }

}
