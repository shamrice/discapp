package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.SiteUpdateLog;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.site.SiteService;
import io.github.shamrice.discapp.web.define.url.AppUrl;
import io.github.shamrice.discapp.web.model.SearchApplicationModel;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private WebHelper webHelper;

    @GetMapping("/")
    public ModelAndView getIndexView(Model model) {

        SiteUpdateLog latestUpdate = siteService.getLatestSiteUpdateLog();
        if (latestUpdate != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-YYYY");
            String updateDate = simpleDateFormat.format(latestUpdate.getCreateDt());

            model.addAttribute("updateDate", updateDate);
            model.addAttribute("updateSubject", latestUpdate.getSubject());
            model.addAttribute("updateMessage", latestUpdate.getMessage());
        }

        return new ModelAndView("home/index", "model", model);
    }

    @GetMapping("/search-apps")
    public ModelAndView getSearchAppsView(@RequestParam String searchValue,
                                          SearchApplicationModel searchApplicationModel,
                                          HttpServletRequest request,
                                          Model model) {


        String baseUrl = webHelper.getBaseUrl(request);
        searchApplicationModel.setBaseUrl(baseUrl);
        model.addAttribute("searchText", searchValue);

        int minSearchLength = configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.HOME_PAGE_SEARCH_MIN_LENGTH, 1);

        Map<Long, SearchApplicationModel.SearchResult> searchResults = new HashMap<>();
        if (searchValue != null && searchValue.trim().length() >= minSearchLength) {
            List<Application> foundApps = applicationService.searchByApplicationName(searchValue.trim());
            if (foundApps != null && foundApps.size() > 0) {
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
                searchApplicationModel.setInfoMessage("Found " + searchResults.size() + " search results.");
            } else {
                searchApplicationModel.setInfoMessage("No results found.");
            }

        } else {
            searchApplicationModel.setInfoMessage("Please enter a search criteria of at least " + minSearchLength + " characters.");
        }
        return new ModelAndView("home/search-apps-results", "searchApplicationModel", searchApplicationModel);
    }

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET, produces = "text/plain")
    public String getRobotsTxt(Model model, HttpServletResponse response) {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        String robotsTxtContents = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.ROBOTS_TXT_CONTENTS, "");
        model.addAttribute("robotsTxtContents", robotsTxtContents);
        return "home/robots";
    }
}
