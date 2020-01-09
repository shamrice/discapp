package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ImportData;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceImportExportViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class ImportExportMaintenanceController extends MaintenanceController {


    @GetMapping(CONTROLLER_URL_DIRECTORY + "data/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadImportFile(@RequestParam(name = "id") long appId) {

        try {
            Application app = applicationService.get(appId);

            ImportData importData = applicationImportService.getImportData(app.getId());

            if (importData != null) {
                Resource file = new ByteArrayResource(importData.getImportData());
                return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + importData.getImportName() + "\"")
                        .body(file);
            }

        } catch (Exception ex) {
            log.error("Error downloading import for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "data/export")
    @ResponseBody
    public ResponseEntity<Resource> postExportFile(@RequestParam(name = "id") long appId,
                                                   MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                                   Model model,
                                                   HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();

            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());

            String filename = applicationExportService.generateExportForApplication(app.getId());
            Resource file = fileSystemStorageService.loadAsResource(filename);
            if (file != null) {
                log.info("Generated export file for appId: " + app.getId());
                return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            } else {
                log.warn("Failed to generate export file for appId: " + app.getId());
                return ResponseEntity.noContent().build();
            }

        } catch (Exception ex) {
            log.error("Error posting maintenance export for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "data/import")
    public ModelAndView postImportFile(@RequestParam(name = "id") long appId,
                                       @RequestParam("uploadSourceFile") MultipartFile uploadSourceFile,
                                       MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                       Model model,
                                       HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());
            String newFilename = "disc_" + app.getId() + ".sql";

            if (applicationImportService.saveImportData(app.getId(), newFilename, uploadSourceFile.getBytes())) {
                log.info("Import for appId: " + app.getId() + " saved successfully to import table.");
                maintenanceImportExportViewModel.setInfoMessage(
                        "Disc App import file successfully uploaded. You will receive an email at your account email address " +
                                "when the import is completed.");

            } else {
                maintenanceImportExportViewModel.setInfoMessage("Failed to upload Disc App import file. Please try again.");
            }

            return getImportExportView(app.getId(), maintenanceImportExportViewModel, model, response);

        } catch (Exception ex) {
            log.error("Error posting maintenance import for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "disc-import-export.cgi")
    public ModelAndView getImportExportView(@RequestParam(name = "id") long appId,
                                            MaintenanceImportExportViewModel maintenanceImportExportViewModel,
                                            Model model,
                                            HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            maintenanceImportExportViewModel.setApplicationId(app.getId());
            return new ModelAndView("admin/disc-import-export", "maintenanceImportExportViewModel", maintenanceImportExportViewModel);

        } catch (Exception ex) {
            log.error("Error getting maintenance import export page for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return new ModelAndView("redirect:/error");
    }

}
