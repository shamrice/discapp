package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Stats;
import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceStatsViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@Slf4j
public class StatsMaintenanceController extends MaintenanceController {

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-stats.cgi")
    public ModelAndView getDiscStatsView(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "selectedStatsId", required = false) Long statsId,
                                         @RequestParam(name = "page", required = false) Integer page,
                                         MaintenanceStatsViewModel maintenanceStatsViewModel,
                                         Model model,
                                         HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            long totalPageViews = 0L;
            long totalUniqueIps = 0L;
            float totalUniqueIpsPerDay = 0;
            int numRecords = 30;

            if (page == null || page < 0) {
                page = 0;
            }
            maintenanceStatsViewModel.setCurrentPage(page);

            String whoIsUrl = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.WHOIS_URL, "https://www.whois.com/whois/");
            maintenanceStatsViewModel.setWhoIsUrl(whoIsUrl);

            List<Stats> stats = statisticsService.getLatestStatsForApp(app.getId(), page, numRecords);

            //if there's less than 30 days of data, do calculations on what we actually have.
            if (stats.size() < numRecords) {
                numRecords = stats.size();
                maintenanceStatsViewModel.setMoreRecords(false);
            } else {
                maintenanceStatsViewModel.setMoreRecords(true);
            }

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -90);
            Date maxStatAge = calendar.getTime();

            List<MaintenanceStatsViewModel.StatView> statViews = new ArrayList<>();

            for (Stats dayStat : stats) {

                boolean isUniqueIpAvailable = true;
                if (dayStat.getCreateDt().before(maxStatAge)) {
                    maintenanceStatsViewModel.setUnavailableStatsPresent(true);
                    isUniqueIpAvailable = false;
                }

                MaintenanceStatsViewModel.StatView statView = new MaintenanceStatsViewModel.StatView(
                        dayStat.getStatDate(),
                        dayStat.getId(),
                        dayStat.getUniqueIps(),
                        dayStat.getPageViews(),
                        isUniqueIpAvailable
                );
                statViews.add(statView);

                totalPageViews += dayStat.getPageViews();
                totalUniqueIps += dayStat.getUniqueIps();
                totalUniqueIpsPerDay += statView.getPagesPerIp();
            }

            maintenanceStatsViewModel.setApplicationId(app.getId());
            maintenanceStatsViewModel.setStatViews(statViews);
            maintenanceStatsViewModel.setTotalPageViews(totalPageViews);
            if (totalPageViews != 0) {
                maintenanceStatsViewModel.setAveragePageViews(totalPageViews / numRecords);
            } else {
                maintenanceStatsViewModel.setAveragePageViews(0);
            }
            if (totalUniqueIps != 0) {
                maintenanceStatsViewModel.setAverageUniqueIps(totalUniqueIps / numRecords);
            } else {
                maintenanceStatsViewModel.setAverageUniqueIps(0);
            }
            if (totalUniqueIpsPerDay != 0) {
                maintenanceStatsViewModel.setAveragePagesPerIp(totalUniqueIpsPerDay / numRecords);
            } else {
                maintenanceStatsViewModel.setAveragePagesPerIp(0);
            }

            if (statsId != null && statsId > 0L) {
                Stats selectedStats = statisticsService.getStats(statsId);
                if (selectedStats != null && selectedStats.getApplicationId().equals(app.getId())) {

                    List<StatsUniqueIps> daysUniqueIps = statisticsService.getUniqueIpsForStatsId(selectedStats.getId());

                    maintenanceStatsViewModel.setSelectedStatId(statsId);
                    maintenanceStatsViewModel.setSelectedDate(selectedStats.getStatDate());
                    maintenanceStatsViewModel.setUniqueIps(daysUniqueIps);
                } else {
                    maintenanceStatsViewModel.setInfoMessage("Error retrieving statistics.");
                }
            }
        } catch (Exception ex) {
            log.error("Error getting stats for appId: " + appId + " :: " + ex.getMessage(), ex);
            maintenanceStatsViewModel.setInfoMessage("Error retrieving statistics.");
        }

        return new ModelAndView("admin/disc-stats", "maintenanceStatsViewModel", maintenanceStatsViewModel);
    }

}
