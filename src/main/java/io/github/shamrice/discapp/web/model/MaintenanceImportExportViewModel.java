package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceImportExportViewModel {

    private long applicationId;
    private String infoMessage;
    private String redirect;

    //MultipartFile uploadSourceFile;
    String uploadImportFile;
    String exportData;

}
