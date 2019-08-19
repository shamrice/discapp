package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.Stats;

import java.util.List;

public class MaintenanceStatsViewModel {

    public static class StatView {

        private String date;
        private long uniqueIps;
        private long pageViews;
        private float pagesPerIp;

        public StatView(String date, long uniqueIps, long pageViews) {
            this.date = date;
            this.uniqueIps = uniqueIps;
            this.pageViews = pageViews;
        }

        public String getDate() {
            return date;
        }

        public long getUniqueIps() {
            return uniqueIps;
        }

        public long getPageViews() {
            return pageViews;
        }

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


    public List<StatView> getStatViews() {
        return statViews;
    }

    public void setStatViews(List<StatView> statViews) {
        this.statViews = statViews;
    }

    public long getTotalPageViews() {
        return totalPageViews;
    }

    public void setTotalPageViews(long totalPageViews) {
        this.totalPageViews = totalPageViews;
    }

    public long getAverageUniqueIps() {
        return averageUniqueIps;
    }

    public void setAverageUniqueIps(long averageUniqueIps) {
        this.averageUniqueIps = averageUniqueIps;
    }

    public long getAveragePageViews() {
        return averagePageViews;
    }

    public void setAveragePageViews(long averagePageViews) {
        this.averagePageViews = averagePageViews;
    }

    public float getAveragePagesPerIp() {
        return averagePagesPerIp;
    }

    public void setAveragePagesPerIp(float averagePagesPerIp) {
        this.averagePagesPerIp = averagePagesPerIp;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }


}
