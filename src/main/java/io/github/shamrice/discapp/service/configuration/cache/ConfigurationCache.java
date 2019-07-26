package io.github.shamrice.discapp.service.configuration.cache;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigurationCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationCache.class);

    private Map<Long, Map<ConfigurationProperty, Configuration>> configurationCacheMap = new ConcurrentHashMap<>();
    private Map<Long, Date> configIdLastRefreshDateList = new ConcurrentHashMap<>();

    private long maxCacheAgeMilliseconds = 900000L; //default 15 min

    public void setMaxCacheAgeMilliseconds(long milliseconds) {
        this.maxCacheAgeMilliseconds = milliseconds;
    }

    public Configuration getFromCache(Long applicationId, ConfigurationProperty configurationProperty) {

        logger.info("Attempting to retreive configuration value from cache. appId: " + applicationId
                + " : configProperty: " + configurationProperty.getPropName());

        Map<ConfigurationProperty, Configuration> appConfigs = configurationCacheMap.get(applicationId);
        if (appConfigs != null) {
            Configuration config = appConfigs.get(configurationProperty);

            if (config != null) {

                //make sure value in config cache isn't stale.
                Date lastRefresh = configIdLastRefreshDateList.get(config.getId());
                if (lastRefresh != null) {
                    logger.info("newDate.getTime():       " + new Date().getTime());
                    logger.info("lastRefresh.getTime():   " + lastRefresh.getTime());
                    logger.info("maxCacheAgeMilliseconds: " + maxCacheAgeMilliseconds);
                }
                if (lastRefresh != null && (new Date().getTime() - lastRefresh.getTime() > maxCacheAgeMilliseconds)) {

                    logger.info("Config property: " + config.getName() + " for appId: " + applicationId
                            + " is stale in config and needs to be refreshed. Returning null.");
                    return null;
                }

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
            configIdLastRefreshDateList.put(configuration.getId(), new Date());
        }
    }

}
