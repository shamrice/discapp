package io.github.shamrice.discapp.service.storage;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileSystemStorageService {

    @Autowired
    private ConfigurationService configurationService;

    private Path saveLocation;

    public boolean store(MultipartFile file, String newFilename) throws Exception {

        refreshLocationConfig();

        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        if (newFilename == null || newFilename.isEmpty()) {
            newFilename = filename;
        }

        if (file.isEmpty()) {
            log.error("Failed  to store empty file " + filename);
            return false;
        }

        if (filename.contains("..")) {
            log.error("Cannot store file with relative path outside current directory: " + filename);
            return false;
        }

        InputStream inputStream = file.getInputStream();
        Path saveLocationPath = saveLocation.resolve(newFilename);
        long bytesCopied = Files.copy(inputStream, saveLocationPath, StandardCopyOption.REPLACE_EXISTING);

        if (bytesCopied > 0L) {
            log.warn("Successfully stored uploaded file to " + saveLocationPath.toAbsolutePath().toString());
            return true;
        } else {
            log.error("Failed to upload file: " + saveLocationPath.toAbsolutePath().toString());
            return false;
        }
    }

    public Resource loadAsResource(String filename) throws MalformedURLException, Exception {
        Path file = load(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new Exception("Could not read file: " + filename);
        }
    }

    private Path load(String filename) {
        refreshLocationConfig();
        return saveLocation.resolve(filename);
    }

    private void refreshLocationConfig() {
        String saveLocationStr = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.IMPORT_UPLOAD_LOCATION, "imports");
        saveLocation = Paths.get(saveLocationStr);
        log.info("Refreshing upload location config :: Using configuration directory: " + saveLocationStr
                + " :: Absolute path: " + saveLocation.toAbsolutePath().toString());

        if (Files.notExists(saveLocation)) {
            try {
                Files.createDirectory(saveLocation);
            } catch (IOException ex) {
                log.error("Failed to create upload directory: " + saveLocation.toAbsolutePath().toString() + " :: " + ex.getMessage(), ex);
            }
        }
    }


}
