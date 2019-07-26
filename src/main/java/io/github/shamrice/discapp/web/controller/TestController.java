package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
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

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/test")
    public String test(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);

        String ownersStr = "";
        for (Owner owner : accountService.listOwners()) {
            ownersStr += "Id: " + owner.getId() + "\nName: " + owner.getFirstName() + " " + owner.getLastName()
                    + "\nphone: " + owner.getPhone() + "\nemail: " + owner.getEmail() + "\nenabled: " + owner.getEnabled()
                    + "\ncreate: " + owner.getCreateDt() + "\nmod: " + owner.getModDt() + "\n\n";
        }
        model.addAttribute("owners", ownersStr);

        String appStr = "";
        for (Application app : applicationService.list()) {
            appStr += "Id: " + app.getId() + " Name: " + app.getName()
                    + " owner_id: " + app.getOwnerId()
                    + " create: " + app.getCreateDt() + " mod: " + app.getModDt();
        }
        model.addAttribute("apps", appStr);

        List<Configuration> configurationList = configurationService.list();
        String configStr = "";
        for (Configuration configuration : configurationList) {
            configStr += "\nConfig Id: " + configuration.getId() + "\napplication id: " + configuration.getApplicationId()
                    + "\nConfig Name: " + configuration.getName()
                    + "\nConfig Value: " + configuration.getValue() + "\nCreate Dt: " + configuration.getCreateDt()
                    + "\nMod Dt: " + configuration.getModDt() + "\n\n";
        }

        model.addAttribute("configs", configStr);

        return "test";
    }
}
