package io.github.shamrice.discapp.service.storage;

import lombok.extern.slf4j.Slf4j;
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

    private final Path SAVE_LOCATION = Paths.get("imports");

    public FileSystemStorageService() {
        if (Files.notExists(SAVE_LOCATION)) {
            try {
                Files.createDirectory(SAVE_LOCATION);
            } catch (IOException ex) {
                log.error("Failed to create upload directory: " + SAVE_LOCATION + " :: " + ex.getMessage(), ex);
            }
        }
    }

    public boolean store(MultipartFile file, String newFilename) throws Exception {

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
        long bytesCopied = Files.copy(inputStream, this.SAVE_LOCATION.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);

        return bytesCopied > 0L;
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
        return SAVE_LOCATION.resolve(filename);
    }
}
