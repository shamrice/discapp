package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class SiteController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private WebHelper webHelper;

    @GetMapping("/")
    public ModelAndView getIndexView(Model model) {
        return new ModelAndView("index", "model", model);
    }

    @GetMapping("/search-apps")
    public ModelAndView getSearchAppsView(@RequestParam String searchValue,
                                          SearchApplicationModel searchApplicationModel,
                                          HttpServletRequest request,
                                          Model model) {


        String baseUrl = webHelper.getBaseUrl(request);
        searchApplicationModel.setBaseUrl(baseUrl);
        model.addAttribute("searchText", searchValue);

        Map<String, String> searchResults = new HashMap<>();
        List<Application> foundApps = applicationService.searchByApplicationName(searchValue);
        if (foundApps != null) {
            for (Application app : foundApps) {
                searchResults.put(app.getName(), baseUrl + "/Indices/" + app.getId().toString() + ".html");
            }
        }
        searchApplicationModel.setSearchResults(searchResults);
        return new ModelAndView("search-apps-results", "searchApplicationModel", searchApplicationModel);
    }

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET, produces = "text/plain")
    public String getRobotsTxt(Model model, HttpServletResponse response) {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        String robotsTxtContents = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.ROBOTS_TXT_CONTENTS, "");
        model.addAttribute("robotsTxtContents", robotsTxtContents);
        return "robots";
    }
}
