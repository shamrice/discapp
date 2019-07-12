package io.github.shamrice.discapp.service.configuration.cache;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationCache {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationCache.class);

    private Map<Long, Map<ConfigurationProperty, Configuration>> configurationCacheMap = new ConcurrentHashMap<>();

    Date lastRefreshDate = new Date();

    public Configuration getFromCache(Long applicationId, ConfigurationProperty configurationProperty) {

        logger.info("Attempting to retreive configuration value from cache. appId: " + applicationId
                + " : configProperty: " + configurationProperty.getPropName());

        Map<ConfigurationProperty, Configuration> appConfigs = configurationCacheMap.get(applicationId);
        if (appConfigs != null) {
            Configuration config = appConfigs.get(configurationProperty);

            if (config != null) {
                logger.info("Found configuration for appId: " + applicationId + " : prop: " + config.getName()
                        + " = " + config.getValue());
                return config;
            }
        }

        logger.info("Configuration does not currently exist in the cache. appId: " + applicationId
                + " : configProperty: " + configurationProperty.getPropName());
        return null;
    }

    public void updateCache(Long applicationId, ConfigurationProperty configurationProperty, Configuration configuration) {
        if (applicationId != null && applicationId > 0 && configuration != null) {
            logger.info("Updating configuration cache with updated value for appId: " + applicationId + " : prop: "
                    + configuration.getName() + " = " + configuration.getValue());

            Map<ConfigurationProperty, Configuration> configs = configurationCacheMap.get(applicationId);
            if (configs == null) {
                configs = new ConcurrentHashMap<>();
            }

            configs.put(configurationProperty, configuration);
            configurationCacheMap.put(applicationId, configs);
        }
    }

    public void refreshCache(List<Configuration> configurations) {

        configurationCacheMap.clear();

        //TODO : think on this more
        //TODO : configurations should be pulled from cache to decrease load put on database.
        for (Configuration configuration : configurations) {
            Long appId = configuration.getApplicationId();
            ConfigurationProperty configurationProperty = ConfigurationProperty.valueOf(configuration.getName());
            //configurationCacheMap.put(appId, )
        }

        lastRefreshDate = new Date();
    }
}
