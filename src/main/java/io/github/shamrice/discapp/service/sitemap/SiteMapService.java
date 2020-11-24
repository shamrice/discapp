package io.github.shamrice.discapp.service.sitemap;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SiteMapService {

    @Autowired
    private ThreadRepository threadRepository;

    private static GenericSiteMap genericSiteMapCache = new GenericSiteMap();

    long cacheDuration = 900000L;
    long cacheLastChecked = 0L;

    public SiteMapService(@Value("${discapp.cache.duration}") Long cacheDuration) {
        log.info("Setting site map cache duration to: " + cacheDuration);
        this.cacheDuration = cacheDuration;
    }

    public GenericSiteMap getLatestGenericSiteMap() {
        long currentTime = new Date().getTime();
        if (currentTime - cacheDuration > cacheLastChecked) {
            log.info("Refreshing latest threads cache with new values.");
            cacheLastChecked = new Date().getTime();

            List<GenericSiteMapItem> latestThreadItems = new ArrayList<>();
            Page<Thread> latestThreads = threadRepository.findAll(
                    PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "modDt"))
            );

            //Date time must be formatted to spec:
            //The date must conform to the W3C DATETIME format (http://www.w3.org/TR/NOTE-datetime).
            // Example: 2005-05-10 Lastmod may also contain a timestamp.
            // Example: 2005-05-10T17:33:30+08:00
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

            latestThreads.forEach((thread) -> {
                latestThreadItems.add(new GenericSiteMapItem(
                        "/Indices/" + thread.getApplicationId() + ".html",
                        "/discussion.cgi?disc=" + thread.getApplicationId() + "&article=" + thread.getId(),
                        dateFormat.format(thread.getModDt())
                ));
            });

            genericSiteMapCache.setGenericSiteMapItems(latestThreadItems);
        } else {
            log.info("Latest threads site map cache is not expired. Using cached value.");
        }
        return genericSiteMapCache;
    }
}
