package io.github.shamrice.discapp.service.application.data;

import io.github.shamrice.discapp.data.model.ImportData;
import io.github.shamrice.discapp.data.repository.ImportDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationImportService {

    @Autowired
    private ImportDataRepository importDataRepository;

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

        Optional<ImportData> existingImportRecord = importDataRepository.findByApplicationId(appId);

        ImportData importRecord = existingImportRecord.orElse(new ImportData());

        importRecord.setApplicationId(appId);
        importRecord.setImportName(filename);
        importRecord.setImportData(fileData);
        importRecord.setCreateDt(new Date());
        if (importDataRepository.save(importRecord) != null) {
            log.info("Successfully created new import data record for appid: " + appId);
            return true;
        }
        log.warn("Failed to save import data record for appId: " + appId + " :: Returned save value was null.");
        return false;

    }
}
