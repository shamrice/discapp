package io.github.shamrice.discapp.service.stats;

import io.github.shamrice.discapp.data.model.Stats;
import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import io.github.shamrice.discapp.data.repository.StatsRepository;
import io.github.shamrice.discapp.data.repository.StatsUniqueIpsRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class QueuedPageViewService {

    private static final LinkedBlockingQueue<PageViewStatistic> statsQueue = new LinkedBlockingQueue<>();

    private static final String STATS_DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private StatsUniqueIpsRepository statsUniqueIpsRepository;

    public static void addStatsToPageViewQueue(PageViewStatistic pageViewStatistic) {

        if (pageViewStatistic == null) {
            log.error("Cannot add null page view stats to queue.");
            return;
        }

        if (!statsQueue.offer(pageViewStatistic)) {
            log.error("Failed to add page view stats to queue: " + pageViewStatistic);
        } else {
            log.info("Successfully added page view statistic to queue for processing: " + pageViewStatistic);
        }
    }

    public void start() {
        Thread queuedPageViewThread = new Thread(this::queuedPageViewThreadRunner);
        queuedPageViewThread.setName(QueuedPageViewService.class.getSimpleName());
        queuedPageViewThread.start();
    }

    public void queuedPageViewThreadRunner() {
        log.info("Starting queued page view service.");

        while (true) {
            try {
                PageViewStatistic pageViewStat = statsQueue.take();
                processPageViewStatistic(pageViewStat);
            } catch (Exception ex) {
                log.error("Failed to process page view stat from queue. " + ex.getMessage(), ex);
            }
        }
    }

    private void processPageViewStatistic(PageViewStatistic pageViewStatistic) {
        String statDate = getCurrentStatDateString();

        if (!InetAddressValidator.getInstance().isValid(pageViewStatistic.getIpAddress())) {
            log.warn("Statistics for IP address: " + pageViewStatistic.getIpAddress() + " is not a valid IP Address. Not updating statistics for appId: " + pageViewStatistic.getApplicationId());
            return;
        }

        Stats currentDayStat = null;
        List<Stats> currentDayStatList = statsRepository.findByApplicationIdAndStatDateOrderByCreateDtDesc(pageViewStatistic.getApplicationId(), statDate);

        //in case there is multiple results, only ever take the first one.
        if (currentDayStatList != null && !currentDayStatList.isEmpty()) {
            currentDayStat = currentDayStatList.get(0);
        }

        if (currentDayStat == null) {
            log.info("Failed to find today's stats for appId: " + pageViewStatistic.getApplicationId() + ". creating new.");
            Stats newStat = new Stats();
            newStat.setApplicationId(pageViewStatistic.getApplicationId());
            newStat.setCreateDt(new Date());
            newStat.setModDt(new Date());
            newStat.setPageViews(0L);
            newStat.setUniqueIps(0L);
            newStat.setStatDate(statDate);

            currentDayStat = statsRepository.save(newStat);
        }

        //get current daily list of unique ip page views.
        List<StatsUniqueIps> uniqueIps = statsUniqueIpsRepository.findByStatsId(currentDayStat.getId());
        long uniqueIpForToday = uniqueIps.size();

        boolean ipFound = false;
        for (StatsUniqueIps statsUniqueIps : uniqueIps) {
            if (statsUniqueIps.getIpAddress().equalsIgnoreCase(pageViewStatistic.getIpAddress())) {
                ipFound = true;
                break;
            }
        }

        //add new one if not found
        if (!ipFound) {
            log.info("Unique ip page view detected for today in appId: " + pageViewStatistic.getApplicationId() + " : adding to stats ip table.");
            StatsUniqueIps newUniqueIp = new StatsUniqueIps();
            newUniqueIp.setIpAddress(pageViewStatistic.getIpAddress());
            newUniqueIp.setStatsId(currentDayStat.getId());
            newUniqueIp.setCreateDt(new Date());
            newUniqueIp.setModDt(new Date());
            statsUniqueIpsRepository.save(newUniqueIp);

            uniqueIpForToday++;
        }

        //update daily stats for app and save
        currentDayStat.setPageViews(currentDayStat.getPageViews() + 1);
        currentDayStat.setUniqueIps(uniqueIpForToday);
        currentDayStat.setModDt(new Date());

        statsRepository.save(currentDayStat);
        log.info("Updated daily stats for appId: " + pageViewStatistic.getApplicationId() + " : currentPage Views = "
                + currentDayStat.getPageViews() + " :: uniqueIps = " + uniqueIpForToday);
    }

    private String getCurrentStatDateString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STATS_DATE_FORMAT);
        return simpleDateFormat.format(new Date());
    }
}
