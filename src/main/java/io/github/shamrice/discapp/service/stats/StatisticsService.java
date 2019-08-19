package io.github.shamrice.discapp.service.stats;

import io.github.shamrice.discapp.data.model.Stats;
import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import io.github.shamrice.discapp.data.repository.StatsRepository;
import io.github.shamrice.discapp.data.repository.StatsUniqueIpsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    private static final String STATS_DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private StatsUniqueIpsRepository statsUniqueIpsRepository;

    public void increaseCurrentPageStats(long applicationId, String sourceIpAddress) {


        String statDate = getCurrentStatDateString();

        Stats currentDayStat = statsRepository.findOneByApplicationIdAndStatDate(applicationId, statDate);

        if (currentDayStat == null) {
            logger.warn("Failed to find today's stats for appId: " + applicationId + ". creating new.");
            Stats newStat = new Stats();
            newStat.setApplicationId(applicationId);
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
            if (statsUniqueIps.getIpAddress().equalsIgnoreCase(sourceIpAddress)) {
                ipFound = true;
                break;
            }
        }

        //add new one if not found
        if (!ipFound) {
            logger.info("Unique ip page view detected for today in appId: " + applicationId + " : adding to stats ip table.");
            StatsUniqueIps newUniqueIp = new StatsUniqueIps();
            newUniqueIp.setIpAddress(sourceIpAddress);
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
        logger.info("Updated daily stats for appId: " + applicationId + " : currentPage Views = "
                + currentDayStat.getPageViews() + " :: uniqueIps = " + uniqueIpForToday);
    }

    public List<Stats> getLatestStatsForApp(long applicationId, int numDays) {

        //query to get latest stats for an application
        Pageable limit = PageRequest.of(0, numDays);
        return statsRepository.findByApplicationIdOrderByCreateDtDesc(
                applicationId,
                limit
        );
    }

    private String getCurrentStatDateString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STATS_DATE_FORMAT);
        return simpleDateFormat.format(new Date());
    }
}
