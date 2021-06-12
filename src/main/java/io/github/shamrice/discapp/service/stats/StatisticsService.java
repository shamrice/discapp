package io.github.shamrice.discapp.service.stats;

import io.github.shamrice.discapp.data.model.Stats;
import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import io.github.shamrice.discapp.data.repository.StatsRepository;
import io.github.shamrice.discapp.data.repository.StatsUniqueIpsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StatisticsService {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private StatsUniqueIpsRepository statsUniqueIpsRepository;


    public void increaseCurrentPageStats(long applicationId, String sourceIpAddress) {
        PageViewStatistic pageViewStatistic = new PageViewStatistic();
        pageViewStatistic.setApplicationId(applicationId);
        pageViewStatistic.setIpAddress(sourceIpAddress);

        QueuedPageViewService.addStatsToPageViewQueue(pageViewStatistic);
    }

    public List<Stats> getLatestStatsForApp(long applicationId, int page, int numRecords) {

        //query to get latest stats for an application
        Pageable limit = PageRequest.of(page, numRecords);
        return statsRepository.findByApplicationIdOrderByCreateDtDesc(
                applicationId,
                limit
        );
    }

    public Stats getStats(long statsId) {
        Optional<Stats> stats = statsRepository.findById(statsId);
        return stats.orElse(null);

    }

    public List<StatsUniqueIps> getUniqueIpsForStatsId(long statsId) {
        return statsUniqueIpsRepository.findByStatsId(statsId);
    }
}
