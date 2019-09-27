package io.github.shamrice.discapp.service.application.cache;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ApplicationCache<T> {

    private Map<Long, T> cacheMap = new ConcurrentHashMap<>();
    private Map<Long, Date> refreshDateList = new ConcurrentHashMap<>();

    private long maxCacheAgeMilliseconds = 900000L; //default 15 min

    private boolean maxCacheAgeSet = false;

    public void setMaxCacheAgeMilliseconds(long milliseconds) {
        if (!maxCacheAgeSet) {
            log.info("Setting max cache age for application cache to: " + milliseconds);
            this.maxCacheAgeMilliseconds = milliseconds;
            maxCacheAgeSet = true;
        }
    }

    public T getFromCache(Long applicationId) {

        log.debug("Attempting to retrieve from application cache. appId: " + applicationId);

        T cachedPrologue = cacheMap.get(applicationId);
        if (cachedPrologue != null) {

            //make sure value in config cache isn't stale.
            Date lastRefresh = refreshDateList.get(applicationId);
            if (lastRefresh != null) {
                log.debug("newDate.getTime():       " + new Date().getTime());
                log.debug("lastRefresh.getTime():   " + lastRefresh.getTime());
                log.debug("maxCacheAgeMilliseconds: " + maxCacheAgeMilliseconds);
            }
            if (lastRefresh != null && (new Date().getTime() - lastRefresh.getTime() > maxCacheAgeMilliseconds)) {

                log.info("application cache entry for appId: " + applicationId
                        + " is stale in cache and needs to be refreshed. Returning null.");
                return null;
            }

            log.debug("Found application cached value for appId: " + applicationId);
            return cachedPrologue;
        }

        log.debug("Entry does not currently exist in the application cache. appId: " + applicationId);
        return null;
    }

    @Synchronized
    public void updateCache(Long applicationId, T entry) {

        if (applicationId != null && applicationId > 0 && entry != null) {
            log.info("Updating application cache with updated value for appId: " + applicationId);

            cacheMap.put(applicationId, entry);
            refreshDateList.put(applicationId, new Date());
        }
    }

}
