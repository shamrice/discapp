package io.github.shamrice.discapp.service.application.data;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ImportData;
import io.github.shamrice.discapp.data.repository.ImportDataRepository;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.notification.NotificationType;
import io.github.shamrice.discapp.service.notification.email.EmailNotificationQueueService;
import io.github.shamrice.discapp.service.notification.email.type.TemplateEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationImportService {

    @Autowired
    private ImportDataRepository importDataRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    public ImportData getImportData(long appId) {
        return importDataRepository.findByApplicationId(appId).orElse(null);
    }

    public boolean saveImportData(long appId, String filename, byte[] fileData) {

        if (filename.isEmpty()) {
            log.error("Import filename cannot be empty for appId: " + appId);
            return false;
        }
        if (fileData.length == 0) {
            log.error("Cannot store empty file file import. AppId: " + appId + " :: importFileName: " +filename);
            return false;
        }

        Application app = applicationService.get(appId);
        if (app == null) {
            log.error("No application associated with appId: " + appId + " to import data for.");
            return false;
        }

        Optional<ImportData> existingImportRecord = importDataRepository.findByApplicationId(appId);

        ImportData importRecord = existingImportRecord.orElse(new ImportData());

        importRecord.setApplicationId(appId);
        importRecord.setImportName(filename);
        importRecord.setImportData(fileData);
        importRecord.setCreateDt(new Date());
        if (importDataRepository.save(importRecord) != null) {
            log.info("Successfully created new import data record for appid: " + appId);

            //send admin an email notification of the uploaded import.
            String adminEmail = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_ADMIN_ADDRESS, null);
            if (adminEmail != null) {
                Map<String, Object> subjectTemplateParams = new HashMap<>();
                subjectTemplateParams.put("APPLICATION_NAME", app.getName());

                Map<String, Object> bodyTemplateParams = new HashMap<>();
                bodyTemplateParams.put("APPLICATION_NAME", app.getName());
                bodyTemplateParams.put("APPLICATION_ID", appId);
                bodyTemplateParams.put("IMPORT_FILE_NAME", filename);

                TemplateEmail email = new TemplateEmail(adminEmail, NotificationType.IMPORT_UPLOADED, bodyTemplateParams, false);
                email.setSubjectTemplateParams(subjectTemplateParams);

                EmailNotificationQueueService.addTemplateEmailToSend(email);
            }

            return true;
        }
        log.warn("Failed to save import data record for appId: " + appId + " :: Returned save value was null.");
        return false;

    }
}
