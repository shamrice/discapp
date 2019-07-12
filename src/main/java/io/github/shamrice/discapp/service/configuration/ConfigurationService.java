package io.github.shamrice.discapp.service.configuration;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.repository.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfigurationService {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private ConfigurationRepository configurationRepository;

    public List<Configuration> list() {
        return configurationRepository.findAll();
    }

    public void setDefaultConfigurationValuesForApplication(Long applicationId) {

        logger.info("Setting up default configuration values for appId: " + applicationId + " in database.");

        Map<ConfigurationProperty, String> configsToSet = new HashMap<>();
        configsToSet.put(ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Author");
        configsToSet.put(ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject");
        configsToSet.put(ConfigurationProperty.EMAIL_LABEL_TEXT, "Email");
        configsToSet.put(ConfigurationProperty.DATE_LABEL_TEXT, "Date");
        configsToSet.put(ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message");
        configsToSet.put(ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview");
        configsToSet.put(ConfigurationProperty.EDIT_BUTTON_TEXT, "Edit");
        configsToSet.put(ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message");
        configsToSet.put(ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Articles");
        configsToSet.put(ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page");
        configsToSet.put(ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
        configsToSet.put(ConfigurationProperty.SHARE_BUTTON_TEXT, "Share");
        configsToSet.put(ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css");
        configsToSet.put(ConfigurationProperty.THREAD_SORT_ORDER, "New");
        configsToSet.put(ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, "false");
        configsToSet.put(ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, "false");
        configsToSet.put(ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, "false");
        configsToSet.put(ConfigurationProperty.THREAD_BREAK_TEXT, " <hr /> ");
        configsToSet.put(ConfigurationProperty.ENTRY_BREAK_TEXT, " - ");
        configsToSet.put(ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, "50");
        configsToSet.put(ConfigurationProperty.HEADER_TEXT, "");
        configsToSet.put(ConfigurationProperty.FOOTER_TEXT, "");
        configsToSet.put(ConfigurationProperty.FAVICON_URL, "/favicon.ico");

        for (ConfigurationProperty configurationProperty : configsToSet.keySet()) {
            String value = configsToSet.getOrDefault(configurationProperty, "");

            Configuration newConfig = new Configuration();
            newConfig.setApplicationId(applicationId);
            newConfig.setName(configurationProperty.getPropName());
            newConfig.setValue(value);
            newConfig.setCreateDt(new Date());
            newConfig.setModDt(new Date());

            configurationRepository.save(newConfig);
            logger.info("Added new default configuration : " + newConfig.getName() + " = " + newConfig.getValue()
                    + " for appId: " + applicationId);
        }
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
