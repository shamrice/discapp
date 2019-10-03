package io.github.shamrice.discapp.service.application.data;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApplicationExportService {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    private Path exportDir;

    public String generateExportForApplication(long appId) throws IOException {
        refreshExportDirectoryConfig();

        File testFile = new File(exportDir.toString() + "/export_" + appId + ".sql");
        if (testFile.createNewFile()) {
            log.info("Created export file: " + testFile.getAbsolutePath() + " : " + testFile.getName());

        }
        byte[] fileData = generateExportFileContents(appId).getBytes();
        Files.write(testFile.toPath(), fileData);
        return testFile.getAbsolutePath();
    }

    private String generateExportFileContents(long appId) {
        List<Thread> allThreads = threadService.getThreads(appId);

        StringBuilder fileDataSb = new StringBuilder("-- NE DiscApp Export\n" +
                "-- AppId: " + appId + "\n" +
                "-- Generated: " + new Date() + "\n\n" +
                "DROP TABLE IF EXISTS disc_" + appId + ";\n\n" +
                "CREATE TABLE disc_" + appId + " ( \n" +
                "  id serial NOT NULL, \n" +
                "  thread_id int NOT NULL, \n" +
                "  application_id int NOT NULL, \n" +
                "  submitter varchar(50) NOT NULL, \n" +
                "  email varchar(60), \n" +
                "  ip_address varchar(64), \n" +
                "  user_agent varchar(150), \n" +
                "  subject varchar(65) NOT NULL, \n" +
                "  deleted bool NOT NULL default false, \n" +
                "  show_email bool NOT NULL default false, \n" +
                "  parent_id int NOT NULL DEFAULT 0, \n" +
                "  discapp_user_id int DEFAULT NULL, \n" +
                "  create_dt TIMESTAMP NOT NULL DEFAULT NOW(), \n" +
                "  mod_dt TIMESTAMP NOT NULL DEFAULT NOW(), \n" +
                "  body varchar(16384), \n" +
                "  is_imported bool DEFAULT false, \n" +
                "  PRIMARY KEY (id) \n" +
                ");\n\n");

        for (Thread thread : allThreads) {

            Long discappUserId = null;
            DiscAppUser user = thread.getDiscAppUser();
            if (user != null) {
                discappUserId = user.getId();
            }

            fileDataSb.append("\nINSERT INTO disc_" + appId + " \n" +
                    " (thread_id, application_id, submitter, email, ip_address, user_agent, subject, deleted, show_email, parent_id, discapp_user_id, create_dt, mod_dt, body) \n" +
                    " values (" + thread.getId() + ", " + thread.getApplicationId() + ", '" + thread.getSubmitter().replaceAll("'", "''") + "', ");

            if (thread.getEmail() != null) {
                fileDataSb.append("'" + thread.getEmail().replaceAll("'", "''") + "', ");
            } else {
                fileDataSb.append(" null,");
            }

            if (thread.getIpAddress() != null) {
                fileDataSb.append(" '" + thread.getIpAddress().replaceAll("'", "''") + "', ");
            } else {
                fileDataSb.append(" null, ");
            }

            if (thread.getUserAgent() != null) {
                fileDataSb.append(" '" + thread.getUserAgent() + "', ");
            } else {
                fileDataSb.append(" null, ");
            }

            fileDataSb.append(" '" + thread.getSubject().replaceAll("'", "''") + "', "
                    + thread.getDeleted()
                    + ", " + thread.isShowEmail() + ", "
                    + thread.getParentId() + ", " + discappUserId +
                    ", '" + thread.getCreateDt() + "', '" + thread.getModDt() + "', ");

            if (thread.getBody() != null) {
                fileDataSb.append("'" + thread.getBody().replaceAll("'", "''") + "');\n");
            } else {
                fileDataSb.append(" null);\n");
            }
        }

        fileDataSb.append("\n\n");

        return fileDataSb.toString();
    }

    private void refreshExportDirectoryConfig() {
        String exportDirStr = configurationService.getStringValue(0L, ConfigurationProperty.EXPORT_DOWNLOAD_LOCATION, "exports");
        exportDir = Paths.get(exportDirStr);
        log.info("Refreshing export directory configuration :: Configured path: " + exportDirStr
                + " :: Absolute path: " + exportDir.toAbsolutePath().toAbsolutePath());
        if (Files.notExists(exportDir)) {
            try {
                Files.createDirectory(exportDir);
            } catch (IOException ex) {
                log.error("Failed to create export directory: " + exportDir.toAbsolutePath().toString() + " :: " + ex.getMessage(), ex);
            }
        }
    }
}
