package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Epilogue;
import io.github.shamrice.discapp.data.model.Prologue;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.web.define.url.AppCustomCssUrl;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@Slf4j
public class AppearanceMaintenanceController extends MaintenanceController {

    @GetMapping(CONTROLLER_URL_DIRECTORY + "appearance-forms.cgi")
    public ModelAndView getAppearanceFormsView(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //app config
            maintenanceViewModel.setApplicationCreateDt(app.getCreateDt());
            maintenanceViewModel.setApplicationModDt(app.getModDt());
            maintenanceViewModel.setApplicationId(app.getId());
            maintenanceViewModel.setApplicationName(app.getName());

            //prologue / epilogue config
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

            //stylesheet config
            String styleSheetUrl = configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "");
            maintenanceViewModel.setStyleSheetUrl(styleSheetUrl);

            maintenanceViewModel.setStyleSheetCustomText(configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_CUSTOM_CONFIGURATION, ""));

            //todo : these strings should be an enum or constant
            String styleSheetStyle = configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_STYLE_SETTING, "default");
            if ("default".equalsIgnoreCase(styleSheetStyle)) {
                maintenanceViewModel.setStyleSheetSelected(getDefaultStyleSheetTypeByUrl(styleSheetUrl));
            } else if ("custom-url".equalsIgnoreCase(styleSheetStyle)) {
                maintenanceViewModel.setStyleSheetSelected("custom-url");
            } else if ("custom-inline".equalsIgnoreCase(styleSheetStyle)) {
                maintenanceViewModel.setStyleSheetSelected("custom-inline");
            }

            //threads config
            String sortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.CREATION.name());
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

            int maxThreadCountPerPage = configurationService.getIntegerValue(appId, ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, 20);
            maintenanceViewModel.setMaxThreadCountPerPage(maxThreadCountPerPage);

            int threadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 15);
            maintenanceViewModel.setThreadDepth(threadDepth);

            int previewLengthTopLevel = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, 320);
            maintenanceViewModel.setPreviewTopLevelLength(previewLengthTopLevel);

            int previewLengthReply = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, 200);
            maintenanceViewModel.setPreviewReplyLength(previewLengthReply);

            //header footer configs
            String headerText = configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, "");
            maintenanceViewModel.setHeader(headerText);

            String footerText = configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, "");
            maintenanceViewModel.setFooter(footerText);

            //label configs
            String authorHeader = configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Author");
            maintenanceViewModel.setAuthorHeader(authorHeader);

            String dateHeader = configurationService.getStringValue(appId, ConfigurationProperty.DATE_LABEL_TEXT, "Date");
            maintenanceViewModel.setDateHeader(dateHeader);

            String emailHeader = configurationService.getStringValue(appId, ConfigurationProperty.EMAIL_LABEL_TEXT, "Email");
            maintenanceViewModel.setEmailHeader(emailHeader);

            String subjectHeader = configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject");
            maintenanceViewModel.setSubjectHeader(subjectHeader);

            String messageHeader = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message");
            maintenanceViewModel.setMessageHeader(messageHeader);

            //buttons config
            String shareButton = configurationService.getStringValue(appId, ConfigurationProperty.SHARE_BUTTON_TEXT, "Share");
            maintenanceViewModel.setShareButton(shareButton);

            String editButton = configurationService.getStringValue(appId, ConfigurationProperty.EDIT_BUTTON_TEXT, "Edit");
            maintenanceViewModel.setEditButton(editButton);

            String returnButton = configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages");
            maintenanceViewModel.setReturnButton(returnButton);

            String previewButton = configurationService.getStringValue(appId, ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview");
            maintenanceViewModel.setPreviewButton(previewButton);

            String postButton = configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message");
            maintenanceViewModel.setPostButton(postButton);

            String previousPageButton = configurationService.getStringValue(appId, ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, "Previous Page");
            maintenanceViewModel.setPreviousPageButton(previousPageButton);

            String nextPageButton = configurationService.getStringValue(appId, ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page");
            maintenanceViewModel.setNextPageButton(nextPageButton);

            String replyButton = configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
            maintenanceViewModel.setReplyButton(replyButton);

            //favicon config
            String favicon = configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico");
            maintenanceViewModel.setFavicon(favicon);

            //hold permissions config
            boolean displayPostHoldMessage = configurationService.getBooleanValue(appId, ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_MESSAGE, true);
            maintenanceViewModel.setDisplayPostHoldMessage(displayPostHoldMessage);

            boolean displayAfterPostHoldMessage = configurationService.getBooleanValue(appId, ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_POST_MESSAGE, true);
            maintenanceViewModel.setDisplayAfterPostHoldMessage(displayAfterPostHoldMessage);

            String displayPostHoldMessageText = configurationService.getStringValue(appId, ConfigurationProperty.HOLD_PERMISSIONS_MESSAGE_TEXT, "New messages posted require admin approval. Your message will appear after it has been approved by an moderator.");
            maintenanceViewModel.setDisplayPostHoldMessageText(displayPostHoldMessageText);

            String displayAfterPostHoldMessageText = configurationService.getStringValue(appId, ConfigurationProperty.HOLD_PERMISSIONS_POST_MESSAGE_TEXT, "Your message will be posted once it has been approved by a moderator.");
            maintenanceViewModel.setDisplayAfterPostHoldMessageText(displayAfterPostHoldMessageText);

        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return new ModelAndView("admin/appearance-forms", "maintenanceViewModel", maintenanceViewModel);

    }

        @GetMapping(CONTROLLER_URL_DIRECTORY + "appearance-frameset.cgi")
    public ModelAndView getAppearanceView(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceViewModel.setApplicationId(app.getId());

        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return new ModelAndView("admin/appearance-frameset", "maintenanceViewModel", maintenanceViewModel);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/application")
    public ModelAndView postModifyApplication(@RequestParam(name = "id") long appId,
                                              @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                              Model model,
                                              HttpServletResponse response) {

        if (maintenanceViewModel.getApplicationName() == null || maintenanceViewModel.getApplicationName().isEmpty()) {
            log.warn("Cannot update application name to an empty string");
            maintenanceViewModel.setInfoMessage("Cannot update disc app name to an empty value.");
            return getAppearanceView(appId, maintenanceViewModel, model, response);
        }

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {
                Application app = applicationService.get(appId);

                log.info("Updating application name of id: " + app.getId() + " from: "
                        + maintenanceViewModel.getApplicationName() + " to: " + app.getName());

                String updatedAppName = inputHelper.sanitizeInput(maintenanceViewModel.getApplicationName());

                app.setName(updatedAppName);
                app.setModDt(new Date());

                applicationService.save(app);

                maintenanceViewModel.setInfoMessage("Successfully updated application name.");

            } else {
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/stylesheet")
    public ModelAndView postModifyStyleSheet(@RequestParam(name = "id") long appId,
                                             @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                             Model model,
                                             HttpServletResponse response) {

        Application app = applicationService.get(appId);

        String styleSheetType = maintenanceViewModel.getStyleSheetSelected();

        if ("custom-inline".equalsIgnoreCase(styleSheetType)) {
            String customStyleSheetText = maintenanceViewModel.getStyleSheetCustomText();
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_STYLE_SETTING, styleSheetType);
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_CUSTOM_CONFIGURATION, customStyleSheetText);

            String styleSheetUrl = AppCustomCssUrl.CUSTOM_CSS_URL_PREFIX + appId + AppCustomCssUrl.CUSTOM_CSS_URL_SUFFIX;
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, styleSheetUrl);

        } else if ("custom-url".equalsIgnoreCase(styleSheetType)) {
            String styleSheetUrl = inputHelper.sanitizeInput(maintenanceViewModel.getStyleSheetUrl());
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_STYLE_SETTING, styleSheetType);
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, styleSheetUrl);
        } else {
            String styleSheetUrl = getDefaultStyleSheetUrlByType(styleSheetType);
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_STYLE_SETTING, "default");
            configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, styleSheetUrl);
        }

        maintenanceViewModel.setInfoMessage("Successfully updated Style Sheet URL.");

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/prologue-epilogue")
    public ModelAndView postModifyPrologueEpilogue(@RequestParam(name = "id") long appId,
                                                   @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                                   Model model,
                                                   HttpServletResponse response) {

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {
                Application app = applicationService.get(appId);

                //update prologue if text has changed.
                Prologue prologue = applicationService.getPrologue(app.getId(), false);

                if (prologue == null) {
                    prologue = new Prologue();
                    prologue.setApplicationId(app.getId());
                    prologue.setCreateDt(new Date());
                    prologue.setText("");
                }

                if (!maintenanceViewModel.getPrologueText().equals(prologue.getText())) {
                    log.info("Updating prologue text of id: " + app.getId());
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
                    log.info("Updating epilogue text of id: " + app.getId());
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
                maintenanceViewModel.setInfoMessage("Logged in user does not exist");
            }
        } else {
            maintenanceViewModel.setInfoMessage("You must be logged in to perform this action.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/threads")
    public ModelAndView postModifyThreads(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        Application app = applicationService.get(appId);

        if (maintenanceViewModel.getThreadSortOrder() == null || maintenanceViewModel.getThreadSortOrder().isEmpty()) {
            maintenanceViewModel.setThreadSortOrder(ThreadSortOrder.CREATION.name());
        } else if (ThreadSortOrder.CREATION.name().equalsIgnoreCase(maintenanceViewModel.getThreadSortOrder())) {
            maintenanceViewModel.setThreadSortOrder(ThreadSortOrder.CREATION.name());
        } else {
            maintenanceViewModel.setThreadSortOrder(ThreadSortOrder.ACTIVITY.name());
        }

        boolean sortOrderSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.THREAD_SORT_ORDER, maintenanceViewModel.getThreadSortOrder());
        boolean expandSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isExpandThreadsOnIndex()));
        boolean previewSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.isPreviewFirstMessageOnIndex()));
        boolean highlightSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, String.valueOf(maintenanceViewModel.isHighlightNewMessages()));
        boolean threadBreakSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.THREAD_BREAK_TEXT, maintenanceViewModel.getThreadBreak());
        boolean entryBreakSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.ENTRY_BREAK_TEXT, maintenanceViewModel.getEntryBreak());
        boolean maxThreadCountSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.getMaxThreadCountPerPage()));
        boolean threadDepthSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, String.valueOf(maintenanceViewModel.getThreadDepth()));
        boolean previewTopLevelSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, String.valueOf(maintenanceViewModel.getPreviewTopLevelLength()));
        boolean previewReplySaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, String.valueOf(maintenanceViewModel.getPreviewReplyLength()));

        if (sortOrderSaved && expandSaved & previewSaved && highlightSaved && threadBreakSaved && entryBreakSaved
                && maxThreadCountSaved && threadDepthSaved && previewTopLevelSaved && previewReplySaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to threads.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to threads.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }

    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/header-footer")
    public ModelAndView postModifyHeaderFooter(@RequestParam(name = "id") long appId,
                                               @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                               Model model,
                                               HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean headerSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.HEADER_TEXT, maintenanceViewModel.getHeader());
        boolean footerSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.FOOTER_TEXT, String.valueOf(maintenanceViewModel.getFooter()));

        if (headerSaved && footerSaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to header and footer.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to header and footer.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/labels")
    public ModelAndView postModifyLabels(@RequestParam(name = "id") long appId,
                                         @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                         Model model,
                                         HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean authorHeaderSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.SUBMITTER_LABEL_TEXT, maintenanceViewModel.getAuthorHeader());
        boolean dateHeaderSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.DATE_LABEL_TEXT, String.valueOf(maintenanceViewModel.getDateHeader()));
        boolean emailSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.EMAIL_LABEL_TEXT, String.valueOf(maintenanceViewModel.getEmailHeader()));
        boolean subjectSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.SUBJECT_LABEL_TEXT, String.valueOf(maintenanceViewModel.getSubjectHeader()));
        boolean messageSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.THREAD_BODY_LABEL_TEXT, String.valueOf(maintenanceViewModel.getMessageHeader()));

        if (authorHeaderSaved && dateHeaderSaved && emailSaved && subjectSaved && messageSaved) {
            maintenanceViewModel.setInfoMessage("Successfully saved changes to labels.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to labels.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/buttons")
    public ModelAndView postModifyButtons(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        Application app = applicationService.get(appId);

        boolean shareButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.SHARE_BUTTON_TEXT, maintenanceViewModel.getShareButton());
        boolean editButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.EDIT_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getEditButton()));
        boolean returnButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReturnButton()));
        boolean previewButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.PREVIEW_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPreviewButton()));
        boolean postButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPostButton()));
        boolean previousPageButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPreviousPageButton()));
        boolean nextPageButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getNextPageButton()));
        boolean replyButtonSaved = configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReplyButton()));

        if (shareButtonSaved && editButtonSaved && returnButtonSaved && previewButtonSaved && postButtonSaved
                && previousPageButtonSaved && nextPageButtonSaved && replyButtonSaved) {

            maintenanceViewModel.setInfoMessage("Successfully saved changes to buttons.");
        } else {
            maintenanceViewModel.setInfoMessage("Failed to save changes to buttons.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/favicon")
    public ModelAndView postModifyFavicon(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {
        //default used if field is blank
        String favicon = "/favicon.ico";

        //get form value if not null.
        if (maintenanceViewModel.getFavicon() != null && !maintenanceViewModel.getFavicon().trim().isEmpty()) {
            favicon = inputHelper.sanitizeInput(maintenanceViewModel.getFavicon());
        }

        //if entered something like "www.somesite.com/favicon.ico", attempt to add http in front
        if (!favicon.startsWith("http://") &&
                !favicon.startsWith("https://") &&
                !favicon.startsWith("/")) {

            //if just want default favicon, add front slash, otherwise attempt to add http
            if (favicon.equalsIgnoreCase("favicon.ico")) {
                favicon = "/favicon.ico";
            } else {
                favicon = "http://" + favicon;
            }
        }

        Application app = applicationService.get(appId);

        if (!configurationService.saveApplicationConfiguration(app.getId(), ConfigurationProperty.FAVICON_URL, favicon)) {
            maintenanceViewModel.setInfoMessage("Failed to update Favicon.");
        } else {
            maintenanceViewModel.setFavicon(favicon);
            maintenanceViewModel.setInfoMessage("Successfully updated Favicon.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }


    @PostMapping(CONTROLLER_URL_DIRECTORY + "modify/holdPermissions")
    public ModelAndView postModifyHoldPermissions(@RequestParam(name = "id") long appId,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model,
                                          HttpServletResponse response) {

        Application app = applicationService.get(appId);

        try {
            boolean isPostDisplaySuccess = configurationService.saveApplicationConfiguration(app.getId(),
                    ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_MESSAGE,
                    String.valueOf(maintenanceViewModel.isDisplayPostHoldMessage()).toLowerCase());
            boolean isAfterPostDisplaySuccess = configurationService.saveApplicationConfiguration(app.getId(),
                    ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_POST_MESSAGE,
                    String.valueOf(maintenanceViewModel.isDisplayAfterPostHoldMessage()).toLowerCase());

            boolean isPostMessageSuccess = configurationService.saveApplicationConfiguration(app.getId(),
                    ConfigurationProperty.HOLD_PERMISSIONS_MESSAGE_TEXT,
                    maintenanceViewModel.getDisplayPostHoldMessageText());

            boolean isAfterPostMessageSuccess = configurationService.saveApplicationConfiguration(app.getId(),
                    ConfigurationProperty.HOLD_PERMISSIONS_POST_MESSAGE_TEXT,
                    maintenanceViewModel.getDisplayAfterPostHoldMessageText());

            if (isPostDisplaySuccess && isAfterPostDisplaySuccess && isPostMessageSuccess && isAfterPostMessageSuccess) {
                maintenanceViewModel.setInfoMessage("Hold permission display settings updated.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save hold permission display settings.");
            }

        } catch (Exception ex) {
            log.error("Failed to update hold appearance config.", ex);
            maintenanceViewModel.setInfoMessage("Failed to update hold permission display settings.");
        }

        return getAppearanceFormsView(appId, maintenanceViewModel, model, response);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/application")
    public ModelAndView getModifyApplication(@RequestParam(name = "id") long appId) {

        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/prologue-epilogue")
    public ModelAndView getModifyPrologueEpilogue(@RequestParam(name = "id") long appId) {

        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/stylesheet")
    public ModelAndView getModifyStyleSheet(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/threads")
    public ModelAndView getModifyThreads(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/header-footer")
    public ModelAndView getModifyHeaderFooter(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/labels")
    public ModelAndView getModifyLabels(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/buttons")
    public ModelAndView getModifyButtons(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/favicon")
    public ModelAndView getModifyFavicon(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    @GetMapping(CONTROLLER_URL_DIRECTORY + "modify/time")
    public ModelAndView getModifyTime(@RequestParam(name = "id") long appId) {
        return new ModelAndView("redirect:/admin/appearance-frameset.cgi?id=" + appId);
    }

    private String getDefaultStyleSheetTypeByUrl(String styleSheetUrl) {

        //todo: this is nasty...

        if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_GRAPH, "/styles/graph2.css"))) {
            return "graph";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_OCEAN, "/styles/ocean.css"))) {
            return "ocean";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_CUTE, "/styles/cute.css"))) {
            return "cute";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_CYAN, "/styles/cyan2.css"))) {
            return "cyan";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_FOREST, "/styles/forest.css"))) {
            return "forest";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_MIDNIGHT, "/styles/midnight.css"))) {
            return "midnight";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_STEELY, "/styles/steely.css"))) {
            return "steely";
        } else if (styleSheetUrl.equals(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_TRADITIONAL, "/styles/traditional.css"))) {
            return "traditional";
        } else {
            log.warn("No default style sheet url found that matches: " + styleSheetUrl);
            return "";
        }
    }

    private String getDefaultStyleSheetUrlByType(String styleSheetType) {

        //todo : types should be an enum somewhere.
        switch (styleSheetType) {
            case "graph":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_GRAPH, "/styles/graph2.css");
            case "ocean":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_OCEAN, "/styles/ocean.css");
            case "cute":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_CUTE, "/styles/cute.css");
            case "cyan":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_CYAN, "/styles/cyan2.css");
            case "forest":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_FOREST, "/styles/forest.css");
            case "midnight":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_MIDNIGHT, "/styles/midnight.css");
            case "steely":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_STEELY, "/styles/steely.css");
            case "traditional":
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.STYLE_SHEET_URL_TRADITIONAL, "/styles/traditional.css");
            default:
                log.warn("No default style sheet type found that matches: " + styleSheetType);
                return "";
        }
    }
}
