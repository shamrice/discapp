package io.github.shamrice.discapp.service.configuration;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.model.UserConfiguration;
import io.github.shamrice.discapp.data.repository.ConfigurationRepository;
import io.github.shamrice.discapp.data.repository.UserConfigurationRepository;
import io.github.shamrice.discapp.service.configuration.cache.ConfigurationCache;
import io.github.shamrice.discapp.service.configuration.cache.UserConfigurationCache;
import io.github.shamrice.discapp.service.configuration.enums.AdminReportFrequency;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private UserConfigurationRepository userConfigurationRepository;

    public static final long SITE_WIDE_CONFIGURATION_APP_ID = 0L;

    public ConfigurationService(@Value("${discapp.cache.duration}") Long cacheDuration) {
        ConfigurationCache.getInstance().setMaxCacheAgeMilliseconds(cacheDuration);
        UserConfigurationCache.getInstance().setMaxCacheAgeMilliseconds(cacheDuration);
    }

    public List<Configuration> list() {
        return configurationRepository.findAll();
    }

    public void setDefaultConfigurationValuesForApplication(Long applicationId) {

        log.info("Setting up default configuration values for appId: " + applicationId + " in database.");

        //todo: pull these default values from properties file
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
        configsToSet.put(ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, "Previous Page");
        configsToSet.put(ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
        configsToSet.put(ConfigurationProperty.SHARE_BUTTON_TEXT, "Share");
        configsToSet.put(ConfigurationProperty.STYLE_SHEET_URL, "/styles/graph2.css");
        configsToSet.put(ConfigurationProperty.STYLE_SHEET_STYLE_SETTING, "default");
        configsToSet.put(ConfigurationProperty.STYLE_SHEET_CUSTOM_CONFIGURATION, "");
        configsToSet.put(ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.ACTIVITY.name());
        configsToSet.put(ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, "true");
        configsToSet.put(ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, "true");
        configsToSet.put(ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, "320");
        configsToSet.put(ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, "200");
        configsToSet.put(ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, "true");
        configsToSet.put(ConfigurationProperty.THREAD_BREAK_TEXT, " <hr /> ");
        configsToSet.put(ConfigurationProperty.ENTRY_BREAK_TEXT, " - ");
        configsToSet.put(ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, "15");
        configsToSet.put(ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, "25");
        configsToSet.put(ConfigurationProperty.HEADER_TEXT, "");
        configsToSet.put(ConfigurationProperty.FOOTER_TEXT, "");
        configsToSet.put(ConfigurationProperty.FAVICON_URL, "/favicon.ico");
        configsToSet.put(ConfigurationProperty.FAVICON_STYLE_SETTING, "favicon-custom-url");
        configsToSet.put(ConfigurationProperty.FAVICON_CUSTOM_URL, "/favicon.ico");
        configsToSet.put(ConfigurationProperty.TIMEZONE_LOCATION, "UTC");
        configsToSet.put(ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");
        configsToSet.put(ConfigurationProperty.WIDGET_SHOW_AUTHOR, "true");
        configsToSet.put(ConfigurationProperty.WIDGET_SHOW_DATE, "false");
        configsToSet.put(ConfigurationProperty.WIDGET_USE_STYLE_SHEET, "true");
        configsToSet.put(ConfigurationProperty.WIDGET_WIDTH, "20");
        configsToSet.put(ConfigurationProperty.WIDGET_WIDTH_UNIT, "em");
        configsToSet.put(ConfigurationProperty.WIDGET_HEIGHT, "18");
        configsToSet.put(ConfigurationProperty.WIDGET_HEIGHT_UNIT, "em");

        //todo : store app base url in config and use that to set default mailing list html values instead of hard coded...
        configsToSet.put(ConfigurationProperty.MAILING_LIST_DESCRIPTION_PAGE_HTML, "Enter your email address in order to receive daily updates.");
        configsToSet.put(ConfigurationProperty.MAILING_LIST_FOLLOW_UP_PAGE_HTML, "A confirmation message has been sent to your address. <br> <br> <a href=\"https://nediscapp.herokuapp.com/Indices/" + applicationId + ".html\">Return to the Message Board</a>");
        configsToSet.put(ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_MESSAGE, "Please click on the link below to confirm your subscription.");
        configsToSet.put(ConfigurationProperty.MAILING_LIST_CONFIRMATION_PAGE_HTML, "You are now subscribed. You will receive updates when new articles are posted. <br> <BR> <a href=\"https://nediscapp.herokuapp.com/Indices/" + applicationId + ".html\">Return to the Message Board</a>");
        configsToSet.put(ConfigurationProperty.MAILING_LIST_UNSUBSCRIBE_PAGE_HTML, "You are unsubscribed from this mailing list. <BR> <BR> <a href=\"https://nediscapp.herokuapp.com/Indices/" + applicationId + ".html\">Return to the Message Board</a>");
        configsToSet.put(ConfigurationProperty.MAILING_LIST_EMAIL_UPDATE_SETTINGS, "all");

        configsToSet.put(ConfigurationProperty.EMAIL_REPLY_NOTIFICATION_ENABLED, "true");

        configsToSet.put(ConfigurationProperty.MAILING_LIST_ADMIN_REPORT_FREQUENCY, AdminReportFrequency.NEVER.name());

        configsToSet.put(ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_MESSAGE, "true");
        configsToSet.put(ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_POST_MESSAGE, "true");
        configsToSet.put(ConfigurationProperty.HOLD_PERMISSIONS_MESSAGE_TEXT, "New messages posted require admin approval. Your message will appear after it has been approved by an moderator.");
        configsToSet.put(ConfigurationProperty.HOLD_PERMISSIONS_POST_MESSAGE_TEXT, "Your message will be posted once it has been approved by a moderator.");

        configsToSet.put(ConfigurationProperty.RSS_BEHAVIOR, "ALL");

        for (ConfigurationProperty configurationProperty : configsToSet.keySet()) {
            String value = configsToSet.getOrDefault(configurationProperty, "");

            Configuration newConfig = new Configuration();
            newConfig.setApplicationId(applicationId);
            newConfig.setName(configurationProperty.getPropName());
            newConfig.setValue(value);
            newConfig.setCreateDt(new Date());
            newConfig.setModDt(new Date());

            configurationRepository.save(newConfig);
            log.info("Added new default configuration : " + newConfig.getName() + " = " + newConfig.getValue()
                    + " for appId: " + applicationId);
        }
    }

    public void setDefaultUserConfigurationValuesForUser(Long discappUserId, boolean isUserAccount) {

        log.info("Setting up default user configuration values for userId: " + discappUserId + " in database.");

        //todo: pull these default values from properties file
        Map<UserConfigurationProperty, String> configsToSet = new HashMap<>();

        // only set read tracking and reply notifications
        // for user accounts as system accounts don't have access to those settings.
        if (isUserAccount) {
            configsToSet.put(UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED, "true");
            configsToSet.put(UserConfigurationProperty.USER_REPLY_NOTIFICATION_ENABLED, "true");
        } else {
            configsToSet.put(UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED, "false");
            configsToSet.put(UserConfigurationProperty.USER_REPLY_NOTIFICATION_ENABLED, "false");
        }

        configsToSet.put(UserConfigurationProperty.USER_TIMEZONE_ENABLED, "false");
        configsToSet.put(UserConfigurationProperty.USER_TIMEZONE_LOCATION, "UTC");

        for (UserConfigurationProperty configurationProperty : configsToSet.keySet()) {
            String value = configsToSet.getOrDefault(configurationProperty, "");

            UserConfiguration newConfig = new UserConfiguration();
            newConfig.setDiscappUserId(discappUserId);
            newConfig.setName(configurationProperty.getPropName());
            newConfig.setValue(value);
            newConfig.setCreateDt(new Date());
            newConfig.setModDt(new Date());

            userConfigurationRepository.save(newConfig);
            log.info("Added new default user configuration : " + newConfig.getName() + " = " + newConfig.getValue()
                    + " for userId: " + discappUserId);
        }
    }

    public String getStringValue(Long applicationId, ConfigurationProperty configurationProperty, String defaultValue) {

        //try to get from cache first:
        Configuration configFromCache = ConfigurationCache.getInstance().getFromCache(applicationId, configurationProperty);
        if (configFromCache != null) {
            return configFromCache.getValue();
        }

        //search database if cache doesn't exist
        Configuration configuration = configurationRepository.findOneByApplicationIdAndName(
                applicationId,
                configurationProperty.getPropName()
        );

        if (configuration != null) {
            log.info("Found configuration property: " + configurationProperty.getPropName()
                    + " for appid: " + applicationId + ". Returning value of: " + configuration.getValue());

            ConfigurationCache.getInstance().updateCache(applicationId, configurationProperty, configuration);
            return configuration.getValue();
        }

        log.info("Unable to find configuration property: " + configurationProperty.getPropName()
                + " for appid: " + applicationId + ". Returning default value of: " + defaultValue);
        return defaultValue;
    }

    public boolean getBooleanValue(long applicationId, ConfigurationProperty configurationProperty, boolean defaultValue) {
        String foundStrVal = getStringValue(applicationId, configurationProperty, "");

        if (foundStrVal != null && !foundStrVal.isEmpty()) {
            try {
                return Boolean.parseBoolean(foundStrVal);
            } catch (NumberFormatException ex) {
                log.error("Unable to parse found config value for " + configurationProperty.getPropName()
                        + " : value: " + foundStrVal + " + for appId: " + applicationId, ex);
            }
        }

        log.info("Failed to find configuration value. Returning default value of: " + defaultValue
                + " : for appId: " + applicationId);
        return defaultValue;
    }

    public int getIntegerValue(long applicationId, ConfigurationProperty configurationProperty, int defaultValue) {
        String foundStrVal = getStringValue(applicationId, configurationProperty, "");

        if (foundStrVal != null && !foundStrVal.isEmpty()) {
            try {
                return Integer.parseInt(foundStrVal);
            } catch (NumberFormatException ex) {
                log.error("Unable to parse found config value for " + configurationProperty.getPropName()
                        + " : value: " + foundStrVal + " + for appId: " + applicationId, ex);
            }
        }

        log.info("Failed to find configuration value. Returning default value of: " + defaultValue
                + " : for appId: " + applicationId);
        return defaultValue;
    }

    public List<String> getStringListValue(long applicationId, ConfigurationProperty configurationProperty, List<String> defaultValue) {
        String foundStrVal = getStringValue(applicationId, configurationProperty, "");

        if (foundStrVal != null && !foundStrVal.trim().isEmpty()) {
            return Arrays.asList(foundStrVal.split(","));
        }

        log.info("Failed to find configuration value. Returning default value of: " + defaultValue
                + " : for appId: " + applicationId);
        return defaultValue;
    }

    public boolean saveConfiguration(ConfigurationProperty configurationProperty, Configuration configuration) {
        if (configuration != null) {
            if (configuration.getName().equalsIgnoreCase(configurationProperty.getPropName())) {
                log.info("Saving valid configuration " + configurationProperty.getPropName() + " : "
                        + configuration.getValue() + " for appId: " + configuration.getApplicationId());
                //always update mod date
                configuration.setModDt(new Date());
                //only update create if it's not set
                if (configuration.getCreateDt() == null) {
                    configuration.setCreateDt(new Date());
                }

                Configuration savedConfig = configurationRepository.save(configuration);
                if (savedConfig != null) {
                    ConfigurationCache.getInstance().updateCache(savedConfig.getApplicationId(), configurationProperty, savedConfig);
                    return true;
                } else {
                    log.error("Failed to save configuration. Value returned was null from save.");
                    return false;
                }

            } else {
                log.error("Configuration property: " + configurationProperty.getPropName()
                        + " does not match property name set in configuration to save: " + configuration.getName()
                        + " for appId: " + configuration.getApplicationId());
                return false;
            }
        }
        log.error("Cannot save null configuration value.");
        return false;
    }

    public boolean saveUserConfiguration(UserConfigurationProperty configurationProperty, UserConfiguration configuration) {
        if (configuration != null) {
            if (configuration.getName().equalsIgnoreCase(configurationProperty.getPropName())) {
                log.info("Saving valid user configuration " + configurationProperty.getPropName() + " : "
                        + configuration.getValue() + " for userId: " + configuration.getDiscappUserId());
                //always update mod date
                configuration.setModDt(new Date());
                //only update create if it's not set
                if (configuration.getCreateDt() == null) {
                    configuration.setCreateDt(new Date());
                }

                UserConfiguration savedConfig = userConfigurationRepository.save(configuration);
                if (savedConfig != null) {
                    UserConfigurationCache.getInstance().updateCache(savedConfig.getDiscappUserId(), configurationProperty, savedConfig);
                    return true;
                } else {
                    log.error("Failed to save user configuration. Value returned was null from save.");
                    return false;
                }

            } else {
                log.error("User Configuration property: " + configurationProperty.getPropName()
                        + " does not match property name set in configuration to save: " + configuration.getName()
                        + " for userId: " + configuration.getDiscappUserId());
                return false;
            }
        }
        log.error("Cannot save null user configuration value.");
        return false;
    }

    public boolean getUserConfigBooleanValue(long discappUserId, UserConfigurationProperty configurationProperty, boolean defaultValue) {
        String foundStrVal = getUserConfigStringValue(discappUserId, configurationProperty, "");

        if (foundStrVal != null && !foundStrVal.isEmpty()) {
            try {
                return Boolean.parseBoolean(foundStrVal);
            } catch (NumberFormatException ex) {
                log.error("Unable to parse found user config value for " + configurationProperty.getPropName()
                        + " : value: " + foundStrVal + " + for userId: " + discappUserId, ex);
            }
        }

        log.info("Failed to find user configuration value. Returning default value of: " + defaultValue
                + " : for userId: " + discappUserId);
        return defaultValue;
    }


    public String getUserConfigStringValue(Long discappUserId, UserConfigurationProperty configurationProperty, String defaultValue) {

        //try to get from cache first:
        UserConfiguration configFromCache = UserConfigurationCache.getInstance().getFromCache(discappUserId, configurationProperty);
        if (configFromCache != null) {
            log.debug("Pulling user configuration from cache for userid: " + discappUserId + " :: "
                    + configurationProperty.getPropName() + " : value: " + configFromCache.getValue());
            return configFromCache.getValue();
        }

        //search database if cache doesn't exist
        UserConfiguration configuration = userConfigurationRepository.findOneByDiscappUserIdAndName(
                discappUserId,
                configurationProperty.getPropName()
        );

        if (configuration != null) {
            log.info("Found user configuration property: " + configurationProperty.getPropName()
                    + " for userId: " + discappUserId + ". Returning value of: " + configuration.getValue());

            UserConfigurationCache.getInstance().updateCache(discappUserId, configurationProperty, configuration);
            return configuration.getValue();
        }

        log.info("Unable to find user configuration property: " + configurationProperty.getPropName()
                + " for userId: " + discappUserId + ". Returning default value of: " + defaultValue);
        return defaultValue;
    }

    public Configuration getConfiguration(long applicationId, String configurationName) {
        return configurationRepository.findOneByApplicationIdAndName(applicationId, configurationName);
    }

    public UserConfiguration getUserConfiguration(long discappUserId, String configurationName) {
        return userConfigurationRepository.findOneByDiscappUserIdAndName(discappUserId, configurationName);
    }

    /**
     * Saves updated or new configuration for an application
     *
     * @param appId    application id
     * @param property configuration property to save
     * @param value    values to save for the configuration
     * @return returns true on success and false on failure.
     */
    public boolean saveApplicationConfiguration(long appId, ConfigurationProperty property, String value) {
        if (value == null) {
            log.warn("Attempted to save null app configuration value for appId: " + appId + " : config property: " + property.getPropName());
            return false;
        }

        Configuration configToUpdate = getConfiguration(appId, property.getPropName());

        if (configToUpdate == null) {
            log.info("Creating new app configuration prop: " + property.getPropName() + " for appId: " + appId);
            configToUpdate = new Configuration();
            configToUpdate.setName(property.getPropName());
            configToUpdate.setApplicationId(appId);
        }

        configToUpdate.setValue(value);

        if (!saveConfiguration(property, configToUpdate)) {
            log.warn("Failed to update app configuration " + property.getPropName() + " of appId: " + appId);
            return false;
        } else {
            log.info("Updated " + property.getPropName() + " for appId: " + appId + " to " + value);
        }

        return true;
    }
}
