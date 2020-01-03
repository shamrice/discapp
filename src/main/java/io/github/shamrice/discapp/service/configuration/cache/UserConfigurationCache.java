package io.github.shamrice.discapp.service.configuration.cache;

import io.github.shamrice.discapp.data.model.UserConfiguration;
import io.github.shamrice.discapp.service.configuration.UserConfigurationProperty;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class UserConfigurationCache {


    private static UserConfigurationCache instance = null;

    private Map<Long, Map<UserConfigurationProperty, UserConfiguration>> configurationCacheMap = new ConcurrentHashMap<>();
    private Map<Long, Date> configIdLastRefreshDateList = new ConcurrentHashMap<>();

    private long maxCacheAgeMilliseconds = 900000L; //default 15 min

    private boolean cacheDurationSet = false;

    private UserConfigurationCache() {}

    public static UserConfigurationCache getInstance() {
        if (instance == null) {
            instance = new UserConfigurationCache();
        }
        return instance;
    }

    public void setMaxCacheAgeMilliseconds(long milliseconds) {
        log.debug("Calling max cache age in user config cache: " + milliseconds);
        if (!cacheDurationSet) {
            log.info("Setting cache duration to: " + milliseconds);
            maxCacheAgeMilliseconds = milliseconds;
            cacheDurationSet = true;
        }
    }

    public UserConfiguration getFromCache(Long discappUserId, UserConfigurationProperty configurationProperty) {

        log.debug("Attempting to retrieve user configuration value from cache. userId: " + discappUserId
                + " : configProperty: " + configurationProperty.getPropName());

        Map<UserConfigurationProperty, UserConfiguration> userConfigs = configurationCacheMap.get(discappUserId);
        if (userConfigs != null) {
            UserConfiguration config = userConfigs.get(configurationProperty);

            if (config != null) {

                //make sure value in config cache isn't stale.
                Date lastRefresh = configIdLastRefreshDateList.get(config.getId());
                if (lastRefresh != null) {
                    log.debug("newDate.getTime():       " + new Date().getTime());
                    log.debug("lastRefresh.getTime():   " + lastRefresh.getTime());
                    log.debug("maxCacheAgeMilliseconds: " + maxCacheAgeMilliseconds);
                }
                if (lastRefresh != null && (new Date().getTime() - lastRefresh.getTime() > maxCacheAgeMilliseconds)) {

                    log.info("User Config property: " + config.getName() + " for userId: " + discappUserId
                            + " is stale in cache and needs to be refreshed. Returning null.");
                    return null;
                }

                log.debug("Found cached user configuration for userId: " + discappUserId + " : prop: " + config.getName()
                        + " = " + config.getValue());
                return config;
            }
        }

        log.debug("User configuration does not currently exist in the cache. userId: " + discappUserId
                + " : configProperty: " + configurationProperty.getPropName());
        return null;
    }

    @Synchronized
    public void updateCache(Long discappUserId, UserConfigurationProperty configurationProperty, UserConfiguration configuration) {
        if (discappUserId != null && discappUserId >= 0 && configuration != null) {
            log.debug("Updating user configuration cache with updated value for userId: " + discappUserId + " : prop: "
                    + configuration.getName() + " = " + configuration.getValue());

            Map<UserConfigurationProperty, UserConfiguration> configs = configurationCacheMap.get(discappUserId);
            if (configs == null) {
                configs = new ConcurrentHashMap<>();
            }

            configs.put(configurationProperty, configuration);
            configurationCacheMap.put(discappUserId, configs);
            configIdLastRefreshDateList.put(configuration.getId(), new Date());
        }
    }
}
