package io.github.shamrice.discapp.service.application.data;

import io.github.shamrice.discapp.data.model.ApplicationFavicon;
import io.github.shamrice.discapp.data.model.ImportData;
import io.github.shamrice.discapp.data.repository.ApplicationFaviconRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationFaviconService {

    @Autowired
    private ApplicationFaviconRepository applicationFaviconRepository;


    public ApplicationFavicon getFaviconData(long appId) {
        return applicationFaviconRepository.findOneByApplicationId(appId).orElse(null);
    }

    public boolean saveFaviconData(long appId, String filename, byte[] fileData) {

        if (filename.isEmpty()) {
            log.error("Favicon filename cannot be empty for appId: " + appId);
            return false;
        }
        if (fileData.length == 0) {
            log.error("Cannot store empty file file favicon. AppId: " + appId + " :: fileName: " +filename);
            return false;
        }

        Optional<ApplicationFavicon> existingFavicon = applicationFaviconRepository.findOneByApplicationId(appId);
        ApplicationFavicon applicationFavicon;
        if (existingFavicon.isPresent()) {
            applicationFavicon = existingFavicon.get();
        } else {
            applicationFavicon = new ApplicationFavicon();
            applicationFavicon.setCreateDt(new Date());
        }

        applicationFavicon.setApplicationId(appId);
        applicationFavicon.setFileName(filename);
        applicationFavicon.setFaviconData(fileData);
        applicationFavicon.setModDt(new Date());
        if (applicationFaviconRepository.save(applicationFavicon).getId() != 0) {
            log.info("Successfully saved favicon data record for appid: " + appId);
            return true;
        }
        log.warn("Failed to save favicon data record for appId: " + appId + " :: Returned save id was null.");
        return false;
    }
}
