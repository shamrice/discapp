package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.ApplicationSubscriptionService;
import io.github.shamrice.discapp.service.application.data.ApplicationExportService;
import io.github.shamrice.discapp.service.application.data.ApplicationImportService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.stats.StatisticsService;
import io.github.shamrice.discapp.service.storage.FileSystemStorageService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.controller.application.DiscAppController;
import io.github.shamrice.discapp.web.define.url.MaintenanceUrl;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;

@Slf4j
public abstract class MaintenanceController {

    public static final String CONTROLLER_URL_DIRECTORY = MaintenanceUrl.CONTROLLER_DIRECTORY_URL;

    protected static final String THREAD_TAB = "threads";
    protected static final String DATE_TAB = "date";
    protected static final String SEARCH_TAB = "search";
    protected static final String POST_TAB = "post";
    protected static final String UNAPPROVED_TAB = "unapproved";

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    protected ConfigurationService configurationService;

    @Autowired
    protected ThreadService threadService;

    @Autowired
    protected StatisticsService statisticsService;

    @Autowired
    protected FileSystemStorageService fileSystemStorageService;

    @Autowired
    protected ApplicationExportService applicationExportService;

    @Autowired
    protected ApplicationImportService applicationImportService;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected ApplicationSubscriptionService applicationSubscriptionService;

    @Autowired
    protected AccountHelper accountHelper;

    @Autowired
    protected InputHelper inputHelper;

    @Autowired
    protected WebHelper webHelper;

    @Autowired
    protected DiscAppController discAppController;


    /**
     * Saves updated or new configuration for an application
     *
     * @param appId    application id
     * @param property configuration property to save
     * @param value    values to save for the configuration
     * @return returns true on success and false on failure.
     */
    protected boolean saveUpdatedConfiguration(long appId, ConfigurationProperty property, String value) {

        if (value == null) {
            log.warn("Attempted to save null configuration value for appId: " + appId + " : config property: " + property.getPropName());
            return false;
        }

        Configuration configToUpdate = configurationService.getConfiguration(appId, property.getPropName());

        if (configToUpdate == null) {
            log.info("Creating new configuration prop: " + property.getPropName() + " for appId: " + appId);
            configToUpdate = new Configuration();
            configToUpdate.setName(property.getPropName());
            configToUpdate.setApplicationId(appId);
        }

        configToUpdate.setValue(value);

        if (!configurationService.saveConfiguration(property, configToUpdate)) {
            log.warn("Failed to update configuration " + property.getPropName() + " of appId: " + appId);
            return false;
        } else {
            log.info("Updated " + property.getPropName() + " for appId: " + appId + " to " + value);
        }

        return true;
    }

    protected void setCommonModelAttributes(Model model, Application app, String username) {

        model.addAttribute(APP_NAME, app.getName());
        model.addAttribute(APP_ID, app.getId());
        model.addAttribute(USERNAME, username);

        if (!username.equals(String.valueOf(app.getId()))) {
            model.addAttribute(IS_USER_ACCOUNT, true);
        }

        if (applicationService.isOwnerOfApp(app.getId(), username)) {
            model.addAttribute(IS_OWNER, true);
        }

    }
}
