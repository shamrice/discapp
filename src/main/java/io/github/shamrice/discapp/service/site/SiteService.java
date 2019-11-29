package io.github.shamrice.discapp.service.site;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.model.SiteUpdateLog;
import io.github.shamrice.discapp.data.repository.SiteUpdateLogRepository;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SiteService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private SiteUpdateLogRepository siteUpdateLogRepository;

    private Date lastUpdateChecked = null;
    private SiteUpdateLog siteUpdateLog = null;

    @Value("${discapp.cache.duration}")
    private Long cacheDuration;

    public boolean saveAndPostUpdateLog(SiteUpdateLog siteUpdateLog) {
        log.info("Saving site update log: " + siteUpdateLog.toString());
        this.siteUpdateLog = siteUpdateLog;
        return siteUpdateLogRepository.save(siteUpdateLog) != null;
    }

    public SiteUpdateLog getLatestSiteUpdateLog() {
        if (lastUpdateChecked == null || (new Date().getTime() - lastUpdateChecked.getTime() > cacheDuration)) {
            log.info("Site update last checked greater than " + cacheDuration + " milliseconds ago. Refreshing.");
            siteUpdateLog = siteUpdateLogRepository.findTopByAndEnabledOrderByCreateDtDesc(true);
            lastUpdateChecked = new Date();
        }
        return siteUpdateLog;
    }

    public List<SiteUpdateLog> getSiteUpdateLogs() {
        return siteUpdateLogRepository.findAll(Sort.by("id").descending());
    }

    public SiteUpdateLog getSiteUpdateLog(long id) {
        return siteUpdateLogRepository.findById(id).orElse(null);
    }

    public void saveUpdateLog(SiteUpdateLog siteUpdateLog) {
        log.info("Saving site update log: " + siteUpdateLog.toString());
        siteUpdateLogRepository.save(siteUpdateLog);
    }

    public void updateDiscAppRobotsTxtBlock(long appId, boolean isBlocked) {
        String blockString = "Disallow: /Indices/" + appId + ".html\n"
                + "Disallow: /indices/" + appId + "\n";

        Configuration robotsConfig = configurationService.getConfiguration(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.ROBOTS_TXT_CONTENTS.getPropName());

        if (robotsConfig != null) {
            if (isBlocked) {
                if (!robotsConfig.getValue().contains(blockString)) {
                    robotsConfig.setValue(robotsConfig.getValue() + blockString);
                    robotsConfig.setModDt(new Date());
                    configurationService.saveConfiguration(ConfigurationProperty.ROBOTS_TXT_CONTENTS, robotsConfig);
                }
            } else {
                if (robotsConfig.getValue().contains(blockString)) {
                    robotsConfig.setValue(robotsConfig.getValue().replace(blockString, ""));
                    robotsConfig.setModDt(new Date());
                    configurationService.saveConfiguration(ConfigurationProperty.ROBOTS_TXT_CONTENTS, robotsConfig);
                }
            }
        }
    }
}
