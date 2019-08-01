package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.repository.DiscAppUserRepository;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.model.MaintenanceViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class DiscAppMaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(DiscAppMaintenanceController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscAppUserRepository discappUserRepository;

    @Autowired
    private ConfigurationService configurationService;



    @GetMapping("/admin/disc-maint.cgi")
    public ModelAndView getMaintenanceView(@RequestParam( name = "id") long appId,
                                           @RequestParam(name = "redirect", required = false) String redirect,
                                           @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                           Model model) {

        maintenanceViewModel.setRedirect(redirect);

        try {

            Application app = applicationService.get(appId);
            String username = new AccountHelper().getLoggedInUserName();

            if (app != null && applicationService.isOwnerOfApp(appId, username)) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                maintenanceViewModel.setApplicationCreateDt(app.getCreateDt());
                maintenanceViewModel.setApplicationModDt(app.getModDt());
                maintenanceViewModel.setApplicationId(app.getId());
                maintenanceViewModel.setApplicationName(app.getName());

                Prologue prologue = applicationService.getPrologue(appId, false);
                if (prologue != null) {
                    maintenanceViewModel.setPrologueModDt(prologue.getModDt());
                    maintenanceViewModel.setPrologueText(prologue.getText());
                }

                Epilogue epilogue = applicationService.getEpilogue(appId, false);
                if (epilogue != null) {
                    maintenanceViewModel.setEpilogueModDt(epilogue.getModDt());
                    maintenanceViewModel.setEpilogueText(epilogue.getText());
                }

                String styleSheetUrl = configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "");
                maintenanceViewModel.setStyleSheetUrl(styleSheetUrl);

                String sortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, "creation");
                maintenanceViewModel.setThreadSortOrder(sortOrder);

                boolean expandThreadsOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, false);
                maintenanceViewModel.setExpandThreadsOnIndex(expandThreadsOnIndex);

                boolean previewFirstMessageOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, false);
                maintenanceViewModel.setPreviewFirstMessageOnIndex(previewFirstMessageOnIndex);

                boolean highlightNewMessages = configurationService.getBooleanValue(appId, ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, false);
                maintenanceViewModel.setHighlightNewMessages(highlightNewMessages);

                String threadBreak = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />");
                maintenanceViewModel.setThreadBreak(threadBreak);

                String entryBreak = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                maintenanceViewModel.setEntryBreak(entryBreak);

                int threadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 15);
                maintenanceViewModel.setThreadDepth(threadDepth);

            } else {
                maintenanceViewModel.setInfoMessage("You do not have permission to edit this disc app.");
                logger.warn("User: " + username + " has attempted to edit disc app id " + appId + ".");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return new ModelAndView("admin/disc-maint", "maintenanceViewModel", maintenanceViewModel);
    }

    @GetMapping("/admin/modify/application")
    public ModelAndView getModifyApplication(@RequestParam(name = "id") long appId,
                                             @RequestParam(name = "redirect", required = false) String redirect) {

        return new ModelAndView("redirect:/admin/disc-maint.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/prologue-epilogue")
    public ModelAndView getModifyPrologueEpilogue(@RequestParam(name = "id") long appId,
                                                  @RequestParam(name = "redirect", required = false) String redirect) {

        return new ModelAndView("redirect:/admin/disc-maint.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/stylesheet")
    public ModelAndView getModifyStyleSheet(@RequestParam(name = "id") long appId,
                                                  @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/disc-maint.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/threads")
    public ModelAndView getModifyThreads(@RequestParam(name = "id") long appId,
                                            @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/disc-maint.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @PostMapping("/admin/modify/application")
    public ModelAndView postModifyApplication(@RequestParam(name = "id") long appId,
                                              @RequestParam(name = "redirect", required = false) String redirect,
                                              @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                              Model model) {

        if (maintenanceViewModel.getApplicationName() == null || maintenanceViewModel.getApplicationName().isEmpty()) {
            logger.warn("Cannot update application name to an empty string");
            maintenanceViewModel.setInfoMessage("Cannot update disc app name to an empty value.");
            return getMaintenanceView(appId, redirect, maintenanceViewModel, model);
        }

        AccountHelper accountHelper = new AccountHelper();
        String username = accountHelper.getLoggedInUserName();

        if (username != null && !username.isEmpty()) {
            DiscAppUser user = discappUserRepository.findByUsername(username);
            if (user != null) {
                Application app = applicationService.get(appId);
                if (app != null && app.getOwnerId().equals(user.getOwnerId())) {

                    logger.info("Updating application name of id: " + app.getId() + " from: "
                            + maintenanceViewModel.getApplicationName() + " to: " + app.getName());

                    app.setName(maintenanceViewModel.getApplicationName());
                    app.setModDt(new Date());

                    applicationService.save(app);

                    maintenanceViewModel.setInfoMessage("Successfully updated application name.");
                } else {
                    maintenanceViewModel.setInfoMessage("User is not the owner of this app. Cannot save changes.");
                }
            } else {
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getMaintenanceView(appId, redirect, maintenanceViewModel, model);

    }

    @PostMapping("/admin/modify/stylesheet")
    public ModelAndView postModifyStyleSheet(@RequestParam(name = "id") long appId,
                                             @RequestParam(name = "redirect", required = false) String redirect,
                                             @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                             Model model) {

        if (maintenanceViewModel.getStyleSheetUrl() == null || maintenanceViewModel.getStyleSheetUrl().isEmpty()) {
            maintenanceViewModel.setInfoMessage("Style sheet URL cannot be empty. Settings not saved.");
            return getMaintenanceView(appId, redirect, maintenanceViewModel, model);
        }

        AccountHelper accountHelper = new AccountHelper();
        String username = accountHelper.getLoggedInUserName();

        if (applicationService.isOwnerOfApp(appId, username)) {
            Application app = applicationService.get(appId);

            if (!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, maintenanceViewModel.getStyleSheetUrl())) {
                maintenanceViewModel.setInfoMessage("Failed to update Style Sheet URL.");
            } else {
                maintenanceViewModel.setInfoMessage("Successfully updated Style Sheet URL.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getMaintenanceView(appId, redirect, maintenanceViewModel, model);
    }

    @PostMapping("/admin/modify/prologue-epilogue")
    public ModelAndView postModifyPrologueEplilogue(@RequestParam(name = "id") long appId,
                                                    @RequestParam(name = "redirect", required = false) String redirect,
                                                    @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                                    Model model) {

        AccountHelper accountHelper = new AccountHelper();
        String username = accountHelper.getLoggedInUserName();

        if (username != null && !username.isEmpty()) {
            DiscAppUser user = discappUserRepository.findByUsername(username);
            if (user != null) {
                Application app = applicationService.get(appId);
                if (app != null && app.getOwnerId().equals(user.getOwnerId())) {

                    //update prologue if text has changed.
                    Prologue prologue = applicationService.getPrologue(app.getId(), false);

                    if (prologue == null) {
                        prologue = new Prologue();
                        prologue.setApplicationId(app.getId());
                        prologue.setCreateDt(new Date());
                        prologue.setText("");
                    }

                    if (!maintenanceViewModel.getPrologueText().equals(prologue.getText())) {
                        logger.info("Updating prologue text of id: " + app.getId());
                        prologue.setModDt(new Date());
                        prologue.setText(maintenanceViewModel.getPrologueText());

                        Prologue saved = applicationService.savePrologue(prologue);
                        if (saved != null) {
                            maintenanceViewModel.setInfoMessage("Prologue updated successfully. ");
                        } else {
                            maintenanceViewModel.setInfoMessage("Failed to save prologue. ");
                        }
                    }

                    Epilogue epilogue = applicationService.getEpilogue(app.getId(), false);

                    if (epilogue == null) {
                        epilogue = new Epilogue();
                        epilogue.setApplicationId(app.getId());
                        epilogue.setCreateDt(new Date());
                        epilogue.setText("");
                    }

                    if (!maintenanceViewModel.getEpilogueText().equals(epilogue.getText())) {
                        logger.info("Updating epilogue text of id: " + app.getId());
                        epilogue.setModDt(new Date());
                        epilogue.setText(maintenanceViewModel.getEpilogueText());

                        Epilogue savedEpilogue = applicationService.saveEpilogue(epilogue);
                        String currentMessage = maintenanceViewModel.getInfoMessage();
                        if (currentMessage == null) {
                            currentMessage = "";
                        }

                        if (savedEpilogue != null) {
                            currentMessage += " Epilogue updated successfully.";
                        } else {
                            currentMessage += " Failed to save epilogue.";
                        }

                        maintenanceViewModel.setInfoMessage(currentMessage);
                    }

                } else {
                    maintenanceViewModel.setInfoMessage("User is not the owner of this app. Cannot save changes.");
                }
            } else {
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getMaintenanceView(appId, redirect, maintenanceViewModel, model);

    }


    @PostMapping("/admin/modify/threads")
    public ModelAndView postModifyThreads(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "redirect", required = false) String redirect,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model) {

        AccountHelper accountHelper = new AccountHelper();
        String username = accountHelper.getLoggedInUserName();

        if (applicationService.isOwnerOfApp(appId, username)) {
            Application app = applicationService.get(appId);


            boolean sortOrderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_SORT_ORDER, maintenanceViewModel.getThreadSortOrder());
            boolean expandSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isExpandThreadsOnIndex()));
            boolean previewSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isPreviewFirstMessageOnIndex()));
            boolean highlightSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, String.valueOf(maintenanceViewModel.isHighlightNewMessages()));
            boolean threadBreakSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_BREAK_TEXT, maintenanceViewModel.getThreadBreak());
            boolean entryBreakSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.ENTRY_BREAK_TEXT, maintenanceViewModel.getEntryBreak());
            boolean threadDepthSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.getThreadDepth()));

            if (sortOrderSaved && expandSaved & previewSaved && highlightSaved && threadBreakSaved && entryBreakSaved && threadDepthSaved) {
                maintenanceViewModel.setInfoMessage("Successfully saved changes to threads.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save changes to threads.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getMaintenanceView(appId, redirect, maintenanceViewModel, model);
    }

    private boolean saveUpdatedConfiguration(long appId, ConfigurationProperty property, String value) {

        Configuration configToUpdate = configurationService.getConfiguration(appId, property.getPropName());

        if (configToUpdate == null) {
            logger.info("Creating new configuration prop: " + property.getPropName() + " for appId: " + appId);
            configToUpdate = new Configuration();
            configToUpdate.setName(property.getPropName());
            configToUpdate.setApplicationId(appId);
        }

        configToUpdate.setValue(value);

        if (!configurationService.saveConfiguration(property, configToUpdate)) {
            logger.warn("Failed to update configuration " + property.getPropName() + " of appId: " + appId);
            return false;
        } else {
            logger.info("Updated " + property.getPropName() + " for appId: " + appId + " to " + value);
        }

        return true;
    }

}
