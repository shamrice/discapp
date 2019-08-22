package io.github.shamrice.discapp.service.configuration.cache;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConfigurationCache {

    private static ConfigurationCache instance = null;

    private Map<Long, Map<ConfigurationProperty, Configuration>> configurationCacheMap = new ConcurrentHashMap<>();
    private Map<Long, Date> configIdLastRefreshDateList = new ConcurrentHashMap<>();

    private long maxCacheAgeMilliseconds = 900000L; //default 15 min

    private ConfigurationCache() {}

    public static ConfigurationCache getInstance() {
        if (instance == null) {
            instance = new ConfigurationCache();
        }
        return instance;
    }

    public void setMaxCacheAgeMilliseconds(long milliseconds) {
        maxCacheAgeMilliseconds = milliseconds;
    }

    public Configuration getFromCache(Long applicationId, ConfigurationProperty configurationProperty) {

        log.debug("Attempting to retrieve configuration value from cache. appId: " + applicationId
                + " : configProperty: " + configurationProperty.getPropName());

        Map<ConfigurationProperty, Configuration> appConfigs = configurationCacheMap.get(applicationId);
        if (appConfigs != null) {
            Configuration config = appConfigs.get(configurationProperty);

            if (config != null) {

                //make sure value in config cache isn't stale.
                Date lastRefresh = configIdLastRefreshDateList.get(config.getId());
                if (lastRefresh != null) {
                    log.debug("newDate.getTime():       " + new Date().getTime());
                    log.debug("lastRefresh.getTime():   " + lastRefresh.getTime());
                    log.debug("maxCacheAgeMilliseconds: " + maxCacheAgeMilliseconds);
                }
                if (lastRefresh != null && (new Date().getTime() - lastRefresh.getTime() > maxCacheAgeMilliseconds)) {

                    log.info("Config property: " + config.getName() + " for appId: " + applicationId
                            + " is stale in cache and needs to be refreshed. Returning null.");
                    return null;
                }

                log.info("Found cached configuration for appId: " + applicationId + " : prop: " + config.getName()
                        + " = " + config.getValue());
                return config;
            }
        }

        log.info("Configuration does not currently exist in the cache. appId: " + applicationId
                + " : configProperty: " + configurationProperty.getPropName());
        return null;
    }

    @Synchronized
    public void updateCache(Long applicationId, ConfigurationProperty configurationProperty, Configuration configuration) {
        if (applicationId != null && applicationId > 0 && configuration != null) {
            log.info("Updating configuration cache with updated value for appId: " + applicationId + " : prop: "
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
