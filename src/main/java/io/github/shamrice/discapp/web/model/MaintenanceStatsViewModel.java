package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MaintenanceStatsViewModel {

    @RequiredArgsConstructor
    @Getter
    public static class StatView {

        private @NonNull String date;
        private @NonNull long statId;
        private @NonNull long uniqueIps;
        private @NonNull long pageViews;

        public float getPagesPerIp() {
            if (uniqueIps != 0) {
                return pageViews / uniqueIps;
            }
            return pageViews;
        }
    }

    private long applicationId;
    private String infoMessage;
    private List<StatView> statViews;
    private long totalPageViews;
    private long averageUniqueIps;
    private long averagePageViews;
    private float averagePagesPerIp;
    private long selectedStatId;
    private String selectedDate;
    private String whoIsUrl;
    private List<StatsUniqueIps> uniqueIps;

}
