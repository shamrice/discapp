package io.github.shamrice.discapp.service.site;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class SiteService {

    @Autowired
    private ConfigurationService configurationService;

    public void updateDiscAppRobotsTxtBlock(long appId, boolean isBlocked) {
        String blockString = "Disallow: /Indices/" + appId + ".html\n"
                + "Disallow: /indices/" + appId + "\n";

        Configuration robotsConfig = configurationService.getConfiguration(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.ROBOTS_TXT_CONTENTS.getPropName());

        if (robotsConfig != null) {
            if (isBlocked) {
                if (!robotsConfig.getValue().contains(blockString)) {
                    robotsConfig.setValue(robotsConfig.getValue() + blockString);
                    robotsConfig.setModDt(new Date());
                    configurationService.saveConfiguration(ConfigurationProperty.ROBOTS_TXT_CONTENTS, robotsConfig);
                }
            } else {
                if (robotsConfig.getValue().contains(blockString)) {
                    robotsConfig.setValue(robotsConfig.getValue().replace(blockString, ""));
                    robotsConfig.setModDt(new Date());
                    configurationService.saveConfiguration(ConfigurationProperty.ROBOTS_TXT_CONTENTS, robotsConfig);
                }
            }
        }
    }
}
