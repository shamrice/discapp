package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.util.InputHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class TestController {

    //TODO : remove this eventually.

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InputHelper inputHelper;

    @GetMapping("/test/test")
    public String test(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);

        String ownersStr = "";

        for (Owner owner : accountService.listOwners()) {
            ownersStr += owner.toString() + "<br /><BR />";
        }
        model.addAttribute("owners", ownersStr);

        String appStr = "";
        for (Application app : applicationService.list()) {
            appStr += app.toString() + "<br /><br />";
        }
        model.addAttribute("apps", appStr);

        List<Configuration> configurationList = configurationService.list();
        model.addAttribute("configs", configurationList);

        return "test";
    }
}
