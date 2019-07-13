package io.github.shamrice.discapp.service.application.cache;

import io.github.shamrice.discapp.data.model.Prologue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationCache<T> {

    private static Logger logger = LoggerFactory.getLogger(ApplicationCache.class);

    private Map<Long, T> cacheMap = new ConcurrentHashMap<>();
    private Map<Long, Date> refreshDateList = new ConcurrentHashMap<>();

    private long maxCacheAgeMilliseconds = 900000L; //default 15 min

    public void setMaxCacheAgeMilliseconds(long milliseconds) {
        this.maxCacheAgeMilliseconds = milliseconds;
    }

    public T getFromCache(Long applicationId) {

        logger.info("Attempting to retrieve from cache. appId: " + applicationId);

        T cachedPrologue = cacheMap.get(applicationId);
        if (cachedPrologue != null) {

            //make sure value in config cache isn't stale.
            Date lastRefresh = refreshDateList.get(applicationId);
            if (lastRefresh != null) {
                logger.info("newDate.getTime():       " + new Date().getTime());
                logger.info("lastRefresh.getTime():   " + lastRefresh.getTime());
                logger.info("maxCacheAgeMilliseconds: " + maxCacheAgeMilliseconds);
            }
            if (lastRefresh != null && (new Date().getTime() - lastRefresh.getTime() > maxCacheAgeMilliseconds)) {

                logger.info("cache entry for appId: " + applicationId
                        + " is stale in cache and needs to be refreshed. Returning null.");
                return null;
            }

            logger.info("Found cached value for appId: " + applicationId);
            return cachedPrologue;
        }

        logger.info("Entry does not currently exist in the cache. appId: " + applicationId);
        return null;
    }

    public void updateCache(Long applicationId, T entry) {

        if (applicationId != null && applicationId > 0 && entry != null) {
            logger.info("Updating cache with updated value for appId: " + applicationId);

            cacheMap.put(applicationId, entry);
            refreshDateList.put(applicationId, new Date());
        }
    }

}
