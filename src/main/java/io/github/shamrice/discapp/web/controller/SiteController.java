package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class SiteController {

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET, produces = "text/plain")
    public String getRobotsTxt(Model model, HttpServletResponse response) {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        String robotsTxtContents = configurationService.getStringValue(0L, ConfigurationProperty.ROBOTS_TXT_CONTENTS, "");
        model.addAttribute("robotsTxtContents", robotsTxtContents);
        return "robots";
    }
}
