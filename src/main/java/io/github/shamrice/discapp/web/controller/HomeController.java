package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.SiteUpdateLog;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.site.SiteService;
import io.github.shamrice.discapp.service.stats.StatisticsService;
import io.github.shamrice.discapp.web.define.url.AccountUrl;
import io.github.shamrice.discapp.web.define.url.AppUrl;
import io.github.shamrice.discapp.web.define.url.MaintenanceUrl;
import io.github.shamrice.discapp.web.model.home.HomeOlderUpdatesViewModel;
import io.github.shamrice.discapp.web.model.home.SearchApplicationModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.shamrice.discapp.web.define.url.HomeUrl.*;

@Controller
@Slf4j
public class HomeController {

    private static final int MAX_SEARCH_RESULTS = 10;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private WebHelper webHelper;

    @Autowired
    private AccountHelper accountHelper;

    @GetMapping(CONTROLLER_URL_DIRECTORY)
    public ModelAndView getIndexView(HttpServletRequest request, Model model) {

        SiteUpdateLog latestUpdate = siteService.getLatestSiteUpdateLog();
        if (latestUpdate != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
            String updateDate = simpleDateFormat.format(latestUpdate.getCreateDt());

            model.addAttribute("updateDate", updateDate);
            model.addAttribute("updateSubject", latestUpdate.getSubject());
            model.addAttribute("updateMessage", latestUpdate.getMessage());
        }

        //get ip address of request
        if (request != null) {
            //check forwarded header for proxy users, if not found, use ip provided.
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            statisticsService.increaseCurrentPageStats(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ipAddress);
        }

        setCommonModelAttributes(model);
        return new ModelAndView("home/index", "model", model);
    }

    @GetMapping(OLDER_UPDATES)
    public ModelAndView getOlderUpdatesView(HomeOlderUpdatesViewModel homeOlderUpdatesViewModel, Model model) {

        List<SiteUpdateLog> updates = siteService.getSiteUpdateLogs();
        if (updates != null) {

            List<HomeOlderUpdatesViewModel.Update> updateList = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

            for (SiteUpdateLog update : updates) {

                //only show enabled updates
                if (update.getEnabled()) {
                    String updateDate = simpleDateFormat.format(update.getCreateDt());

                    HomeOlderUpdatesViewModel.Update updateModel = new HomeOlderUpdatesViewModel.Update();
                    updateModel.setDate(updateDate);
                    updateModel.setSubject(update.getSubject());
                    updateModel.setMessage(update.getMessage());

                    updateList.add(updateModel);
                }
            }

            homeOlderUpdatesViewModel.setUpdateList(updateList);
        }

        setCommonModelAttributes(model);

        return new ModelAndView("home/olderUpdates", "model", homeOlderUpdatesViewModel);
    }

    @GetMapping(SEARCH_APPS)
    public ModelAndView getSearchAppsView(@RequestParam String searchValue,
                                          @RequestParam(name = "page", required = false) String pageNumStr,
                                          SearchApplicationModel searchApplicationModel,
                                          HttpServletRequest request,
                                          Model model) {

        String baseUrl = webHelper.getBaseUrl(request);
        searchApplicationModel.setBaseUrl(baseUrl);

        if (searchValue != null && !searchValue.isBlank()) {
            searchApplicationModel.setSearchText(searchValue);
        }

        model.addAttribute("searchText", searchValue);

        int minSearchLength = configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.HOME_PAGE_SEARCH_MIN_LENGTH, 1);

        Map<Long, SearchApplicationModel.SearchResult> searchResults = new HashMap<>();
        if (searchValue != null && searchValue.trim().length() >= minSearchLength) {

            int pageNum = 0;
            if (pageNumStr != null) {
                try {
                    pageNum = Integer.parseInt(pageNumStr);
                } catch (NumberFormatException ex) {
                    log.warn("Invalid page number attempted on home page search: " + pageNumStr + " : Defaulting to page 0.");
                }
            }

            if (pageNum < 0) {
                pageNum = 0;
            }

            if (pageNum != 0) {
                searchApplicationModel.setHasPrevious(true);
            }

            searchApplicationModel.setPageNum(pageNum);

            List<Application> foundApps = applicationService.searchByApplicationName(searchValue.trim(), pageNum, MAX_SEARCH_RESULTS);

            if (foundApps != null && foundApps.size() > 0) {

                if (foundApps.size() >= MAX_SEARCH_RESULTS) {
                    searchApplicationModel.setHasNext(true);
                }

                for (Application app : foundApps) {

                    SearchApplicationModel.SearchResult searchResult = new SearchApplicationModel.SearchResult(
                            app.getName(),
                            baseUrl + AppUrl.CONTROLLER_DIRECTORY_URL_ALTERNATE
                                    + app.getId().toString() + AppUrl.APP_NUMBER_SUFFIX_ALTERNATE);
                    searchResults.put(app.getId(), searchResult);
                }

                searchResults = searchResults.entrySet().stream().sorted(
                        Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new)
                        );

                searchApplicationModel.setSearchResults(searchResults);

                long numResults = applicationService.countByApplicationSearchName(searchValue.trim());
                searchApplicationModel.setInfoMessage("Found " + numResults + " search results.");
            } else {
                searchApplicationModel.setInfoMessage("No results found.");
            }

        } else {
            searchApplicationModel.setInfoMessage("Please enter a search criteria of at least " + minSearchLength + " characters.");
        }

        setCommonModelAttributes(model);
        return new ModelAndView("home/search-apps-results", "searchApplicationModel", searchApplicationModel);
    }

    @RequestMapping(value = ROBOTS_TXT, method = RequestMethod.GET, produces = "text/plain")
    public String getRobotsTxt(Model model, HttpServletResponse response) {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        String robotsTxtContents = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.ROBOTS_TXT_CONTENTS, "");
        model.addAttribute("robotsTxtContents", robotsTxtContents);
        return "home/robots";
    }

    private void setCommonModelAttributes(Model model) {
        DiscAppUserPrincipal user = accountHelper.getLoggedInUser();
        if (user != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isUserAccount", user.isUserAccount());
            if (!user.isUserAccount()) {
                model.addAttribute("discAdminUrl", MaintenanceUrl.MAINTENANCE_PAGE + "?id=" + user.getEmail());
            } else {
                model.addAttribute("discAdminUrl", AccountUrl.ACCOUNT_APPLICATION);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isUserAccount", false);
        }
    }
}
