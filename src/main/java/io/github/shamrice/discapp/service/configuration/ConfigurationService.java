package io.github.shamrice.discapp.service.configuration;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.repository.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ConfigurationService {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);



    @Autowired
    private ConfigurationRepository configurationRepository;

    public List<Configuration> list() {
        return configurationRepository.findAll();
    }

    public String getStringValue(Long applicationId, ConfigurationProperty configurationProperty, String defaultValue) {

        Configuration configuration = configurationRepository.findOneByApplicationIdAndName(
                applicationId,
                configurationProperty.getPropName()
        );

        if (configuration != null) {
            logger.info("Found configuration property: " + configurationProperty.getPropName()
                    + " for appid: " + applicationId + ". Returnning value of: " + configuration.getValue());
            return configuration.getValue();
        }

        logger.info("Unable to find configuration property: " + configurationProperty.getPropName()
                + " for appid: " + applicationId + ". Returnning default value of: " + defaultValue);
        return defaultValue;
    }

    public void saveConfiguration(ConfigurationProperty configurationProperty, Configuration configuration) {
        if (configuration != null) {
            if (configuration.getName().equalsIgnoreCase(configurationProperty.getPropName())) {
                logger.info("Saving valid configuration " + configurationProperty.getPropName() + " : "
                        + configuration.getValue() + " for appId: " + configuration.getApplicationId());
                //always update mod date
                configuration.setModDt(new Date());
                //only update create if it's not set
                if (configuration.getCreateDt() == null) {
                    configuration.setCreateDt(new Date());
                }

                configurationRepository.save(configuration);
                return;
            } else {
                logger.error("Configuration property: " + configurationProperty.getPropName()
                        + " does not match property name set in configuration to save: " + configuration.getName()
                        + " for appId: " + configuration.getApplicationId());
                return;
            }
        }
        logger.error("Cannot save null configuration value.");
    }
}
