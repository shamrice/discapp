package io.github.shamrice.discapp.web.controller.application;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static io.github.shamrice.discapp.web.define.url.AppCustomCssUrl.CUSTOM_CSS_URL_PREFIX;
import static io.github.shamrice.discapp.web.define.url.AppCustomCssUrl.CUSTOM_CSS_URL_SUFFIX;
import static io.github.shamrice.discapp.web.define.url.HomeUrl.ROBOTS_TXT;

@Controller
@Slf4j
public class CustomStyleSheetController {

    @Autowired
    private ConfigurationService configurationService;


    @RequestMapping(value = CUSTOM_CSS_URL_PREFIX + "{appId}" + CUSTOM_CSS_URL_SUFFIX,
            method = RequestMethod.GET, produces = "text/css")
    public String getCustomCss(@PathVariable(name = "appId") Long appId,
                               Model model,
                               HttpServletResponse response) {

        response.setContentType("text/css");
        response.setCharacterEncoding("UTF-8");
        String cssContents = configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_CUSTOM_CONFIGURATION, "");
        model.addAttribute("cssContents", cssContents);
        return "indices/css";
    }
}
