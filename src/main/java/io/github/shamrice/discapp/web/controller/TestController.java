package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.util.InputHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TestController {

    //TODO : remove this eventually.

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

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
            ownersStr += "Id: " + owner.getId() + "<br>Name: " + owner.getFirstName() + " " + owner.getLastName()
                    + "<br>phone: " + owner.getPhone() + "<br>email: " + owner.getEmail() + "<br>enabled: " + owner.getEnabled()
                    + "<br>create: " + owner.getCreateDt() + "<br>mod: " + owner.getModDt() + "<br /><br />";
        }
        model.addAttribute("owners", ownersStr);

        String appStr = "";
        for (Application app : applicationService.list()) {
            appStr += "Id: " + app.getId() + "<br> Name: " + app.getName()
                    + "<br> owner_id: " + app.getOwnerId()
                    + "<br> create: " + app.getCreateDt() + "<br> mod: " + app.getModDt() + "<br><br>";
        }
        model.addAttribute("apps", appStr);

        List<Configuration> configurationList = configurationService.list();
        String configStr = "";
        for (Configuration configuration : configurationList) {
            configStr += "Config Id: " + configuration.getId() + "<br>application id: " + configuration.getApplicationId()
                    + "<br>Config Name: " + configuration.getName()
                    + "<br>Config Value: " + inputHelper.sanitizeInput(configuration.getValue()) + "<br>Create Dt: " + configuration.getCreateDt()
                    + "<br>Mod Dt: " + configuration.getModDt() + "<br><br>";
        }

        model.addAttribute("configs", configStr);

        return "test";
    }
}
