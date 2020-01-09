package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.widget.WidgetViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static io.github.shamrice.discapp.web.define.url.WidgetUrl.WIDGET_VIEW_URL;

@Controller
@Slf4j
public class WidgetController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ThreadService threadService;

    @GetMapping(WIDGET_VIEW_URL)
    public ModelAndView getWidgetView(@RequestParam(name = "disc") long appId,
                                      WidgetViewModel widgetViewModel,
                                      Model model) {

        boolean showAuthor = configurationService.getBooleanValue(appId, ConfigurationProperty.WIDGET_SHOW_AUTHOR, true);
        boolean showDate = configurationService.getBooleanValue(appId, ConfigurationProperty.WIDGET_SHOW_DATE, false);
        boolean useStyleSheet = configurationService.getBooleanValue(appId, ConfigurationProperty.WIDGET_USE_STYLE_SHEET, true);

        String faviconUrl = configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico");

        widgetViewModel.setApplicationId(appId);
        widgetViewModel.setFaviconUrl(faviconUrl);

        if (useStyleSheet) {
            widgetViewModel.setStyleSheetUrl(
                    configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "")
            );
        }

        List<Thread> latestThreads = threadService.getLatestThreads(appId, 0, 5);

        if (latestThreads != null && latestThreads.size() > 0) {
            widgetViewModel.setThreadsHtml(
                    getThreadsHtml(
                            latestThreads,
                            showAuthor,
                            showDate
                    )
            );
        }

        return new ModelAndView("widget/disc-widget", "model", model);
    }

    private List<String> getThreadsHtml(List<Thread> threads, boolean showAuthor, boolean showDate) {

        List<String> threadHtml = new ArrayList<>();
        for (Thread thread : threads) {
            String html = "" +
                    "        <td class=\"link_cell\">" +
                    "           <a  target=\"_blank\"  href=\"/discussion.cgi?disc=" +
                                    thread.getApplicationId() +
                                    "&amp;article=" + thread.getId() + "\">" +
                                    thread.getSubject() +
                    "           </a>" +
                            "</td>";

            if (showAuthor) {
                html += "        <td class=\"author_cell\">" + thread.getSubmitter() +"</td>";
            }

            if (showDate) {
                html += "       <td class=\"date_cell\">" +
                        getAdjustedDateStringForConfiguredTimeZone(
                                thread.getApplicationId(),
                                thread.getCreateDt()) +
                        "</td>";
            }

            threadHtml.add(html);
        }

        return threadHtml;
    }

    private String getAdjustedDateStringForConfiguredTimeZone(long appId, Date date) {

        String timeZoneLocation = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");

        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneLocation));

        return dateFormat.format(date);
    }
}
