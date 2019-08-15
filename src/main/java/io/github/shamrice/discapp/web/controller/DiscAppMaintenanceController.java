package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.MaintenanceThreadViewModel;
import io.github.shamrice.discapp.web.model.MaintenanceViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
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

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class DiscAppMaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(DiscAppMaintenanceController.class);

    private static final String THREAD_TAB = "threads";
    private static final String DATE_TAB = "date";
    private static final String SEARCH_TAB = "search";
    private static final String POST_TAB = "post";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private DiscAppController discAppController;

    @GetMapping("/admin/disc-maint.cgi")
    public ModelAndView getDiscMaintenanceView(@RequestParam(name = "id") long appId,
                                               Model model) {
        model.addAttribute("appName", "");
        model.addAttribute("appId", appId);
        return new ModelAndView("admin/disc-maint");
    }

    @GetMapping("/admin/disc-toolbar.cgi")
    public ModelAndView getDiscToolbarView(@RequestParam(name = "id") long appId,
                                           Model model) {
        model.addAttribute("appName", "");
        model.addAttribute("appId", appId);
        return new ModelAndView("admin/disc-toolbar");
    }

    @GetMapping("/admin/disc-info.cgi")
    public ModelAndView getDiscInfoView(@RequestParam(name = "id") long appId,
                                        Model model) {
        model.addAttribute("appName", "");
        model.addAttribute("appId", appId);
        return new ModelAndView("admin/disc-info");
    }

    @PostMapping("/admin/disc-edit.cgi")
    public ModelAndView postDiscEditView(HttpServletRequest request,
                                         @RequestParam(name = "id") long appId,
                                         @RequestParam(name = "tab", required = false) String currentTab,
                                         @RequestParam(name = "pagemark", required = false) Long pageMark,
                                         @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                         Model model) {


        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();

            if (app != null && applicationService.isOwnerOfApp(appId, username)) {

                //delete threads
                if (maintenanceThreadViewModel.getDeleteArticles() != null && !maintenanceThreadViewModel.getDeleteArticles().isEmpty()) {

                    boolean deleteThreadsSuccess = true;

                    //delete from edit message screen
                    if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                        if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), false)) {
                            logger.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                            deleteThreadsSuccess = false;
                        }
                    } else {
                        //delete from thread view or search view with checkboxes.
                        if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                            for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                                long threadId = Long.parseLong(threadIdStr);
                                if (!threadService.deleteThread(app.getId(), threadId, false)) {
                                    logger.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId());
                                    deleteThreadsSuccess = false;
                                }
                            }
                        }
                    }
                    //set message for user
                    if (deleteThreadsSuccess) {
                        maintenanceThreadViewModel.setInfoMessage("Successfully deleted messages.");
                    } else {
                        maintenanceThreadViewModel.setInfoMessage("Failed to delete messages.");
                    }
                }

                //delete threads and replies
                if (maintenanceThreadViewModel.getDeleteArticlesAndReplies() != null && !maintenanceThreadViewModel.getDeleteArticlesAndReplies().isEmpty()) {

                    boolean deleteThreadsAndRepliesSuccess = true;

                    //delete from edit message screen
                    if (maintenanceThreadViewModel.isOnEditMessage() &&  maintenanceThreadViewModel.getEditArticleId() != null) {
                        if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), true)) {
                            logger.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId() + " and replies.");
                            deleteThreadsAndRepliesSuccess = false;
                        }
                    } else {
                        if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                            for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                                long threadId = Long.parseLong(threadIdStr);
                                if (!threadService.deleteThread(app.getId(), threadId, true)) {
                                    logger.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId() + " and replies.");
                                    deleteThreadsAndRepliesSuccess = false;
                                }
                            }
                        }
                    }

                    if (deleteThreadsAndRepliesSuccess) {
                        maintenanceThreadViewModel.setInfoMessage("Successfully deleted messages and replies.");
                    } else {
                        maintenanceThreadViewModel.setInfoMessage("Failed to delete messages and replies.");
                    }
                }

                //report abuse
                if (maintenanceThreadViewModel.getReportAbuse() != null && !maintenanceThreadViewModel.getReportAbuse().isEmpty()) {
                    DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                    boolean threadsReportedSuccess = true;

                    //report abuse from edit message screen
                    if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                        if (!threadService.reportThreadForAbuse(app.getId(), maintenanceThreadViewModel.getEditArticleId(), user.getId())) {
                            logger.error("Failed to report thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                            threadsReportedSuccess = false;
                        }
                    } else {

                        if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                            for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                                long threadId = Long.parseLong(threadIdStr);
                                if (!threadService.reportThreadForAbuse(app.getId(), threadId, user.getId())) {
                                    logger.error("Failed to report thread id: " + threadId + " for appId: " + app.getId());
                                    threadsReportedSuccess = false;
                                }
                            }
                        }
                    }

                    if (threadsReportedSuccess) {
                        maintenanceThreadViewModel.setInfoMessage("Successfully reported and deleted messages.");
                    } else {
                        maintenanceThreadViewModel.setInfoMessage("Failed to report and delete messages.");
                    }
                }

                //search messages
                if (maintenanceThreadViewModel.getFindMessages() != null && !maintenanceThreadViewModel.getFindMessages().isEmpty()) {

                    List<Thread> searchResults = threadService.searchThreadsByFields(
                            app.getId(),
                            maintenanceThreadViewModel.getAuthorSearch(),
                            maintenanceThreadViewModel.getEmailSearch(),
                            maintenanceThreadViewModel.getSubjectSearch(),
                            maintenanceThreadViewModel.getIpSearch(),
                            maintenanceThreadViewModel.getMessageSearch()
                    );

                    String searchResultsHtml = getSearchThreadHtml(searchResults);
                    List<String> threadHtml = new ArrayList<>();
                    threadHtml.add(searchResultsHtml);
                    maintenanceThreadViewModel.setEditThreadTreeHtml(threadHtml);
                    maintenanceThreadViewModel.setNumberOfMessages(searchResults.size());
                    maintenanceThreadViewModel.setSearchSubmitted(true);
                }

                //search again
                if (maintenanceThreadViewModel.getSearchAgain() != null && !maintenanceThreadViewModel.getSearchAgain().isEmpty()) {
                    maintenanceThreadViewModel.setSearchSubmitted(false);
                    maintenanceThreadViewModel.setTab(SEARCH_TAB);
                }

                //post new message
                if (maintenanceThreadViewModel.getPostArticle() != null && !maintenanceThreadViewModel.getPostArticle().isEmpty()) {

                    DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                    if (user != null && maintenanceThreadViewModel.getNewThreadSubject() != null && !maintenanceThreadViewModel.getNewThreadSubject().trim().isEmpty()) {

                        String subject = inputHelper.sanitizeInput(maintenanceThreadViewModel.getNewThreadSubject());

                        Thread newThread = new Thread();
                        newThread.setSubmitter(user.getUsername());
                        newThread.setDiscappUserId(user.getId());
                        newThread.setParentId(0L);
                        newThread.setShowEmail(user.getShowEmail());
                        newThread.setEmail(user.getEmail());
                        newThread.setDeleted(false);
                        newThread.setApplicationId(app.getId());
                        newThread.setSubject(subject);
                        newThread.setModDt(new Date());
                        newThread.setCreateDt(new Date());

                        //set ip address
                        if (request != null) {
                            //check forwarded header for proxy users, if not found, use ip provided.
                            String ipAddress = request.getHeader("X-FORWARDED-FOR");
                            if (ipAddress == null || ipAddress.isEmpty()) {
                                ipAddress = request.getRemoteAddr();
                            }
                            newThread.setIpAddress(ipAddress);
                        }

                        String body = maintenanceThreadViewModel.getNewThreadMessage();
                        if (body != null && !body.isEmpty()) {
                            body = body.replaceAll("\r", "<br />");

                            body = inputHelper.addUrlHtmlLinksToString(body);
                        }

                        threadService.createNewThread(newThread, body);

                    }

                    //return to thread tab.
                    maintenanceThreadViewModel.setTab(THREAD_TAB);
                    currentTab = THREAD_TAB;
                }

            }
        } catch (Exception ex) {
            logger.error("Thread administration action failed: " + ex.getMessage(), ex);
            maintenanceThreadViewModel.setInfoMessage("Error has occurred. Please try again.");
        }

        maintenanceThreadViewModel.setOnEditMessage(false);
        return getDiscEditView(appId, currentTab, maintenanceThreadViewModel, model);
    }

    @GetMapping("/admin/disc-edit.cgi")
    public ModelAndView getDiscEditView(@RequestParam(name = "id") long appId,
                                        @RequestParam(name = "tab", required = false) String currentTab,
                                        @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                        Model model) {
        model.addAttribute("appName", "");
        model.addAttribute("appId", appId);

        if (currentTab == null || currentTab.isEmpty()) {
            currentTab = THREAD_TAB;
        }

        maintenanceThreadViewModel.setTab(currentTab);

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            model.addAttribute("username", username);

            if (app != null && applicationService.isOwnerOfApp(appId, username)) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());
                maintenanceThreadViewModel.setApplicationId(app.getId());

                DiscAppUser user = discAppUserDetailsService.getByEmail(username);
                model.addAttribute("postingUsername", user.getUsername());

                if (!maintenanceThreadViewModel.getTab().equals(SEARCH_TAB)) {
                    //get edit threads html
                    List<String> threadTreeHtml = new ArrayList<>();
                    List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), 50);

                    if (maintenanceThreadViewModel.getTab().equals(THREAD_TAB)) {
                        for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                            String currentHtml = getEditThreadHtml(threadTreeNode, "<ul>");
                            currentHtml += "</ul>";
                            threadTreeHtml.add(currentHtml);
                        }
                    } else if (maintenanceThreadViewModel.getTab().equals(DATE_TAB)) {
                        String currentHtml = getEditThreadListHtml(threadTreeNodeList);
                        threadTreeHtml.add(currentHtml);
                    }

                    maintenanceThreadViewModel.setEditThreadTreeHtml(threadTreeHtml);
                    maintenanceThreadViewModel.setNumberOfMessages(threadService.getTotalThreadCountForApplicationId(app.getId()));
                }

            } else {
                //TODO : redirect users who don't have permission to view page to a permission denied page or something.
                maintenanceThreadViewModel.setInfoMessage("You do not have permission to edit this disc app.");
                logger.warn("User: " + username + " has attempted to edit disc app id " + appId + ".");
            }
        } catch (Exception ex) {
            logger.error("Error: " + ex.getMessage(), ex);
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }


        return new ModelAndView("admin/disc-edit", "maintenanceThreadViewModel", maintenanceThreadViewModel);
    }

    @GetMapping("/admin/edit-thread.cgi")
    public ModelAndView getEditThreadView(@RequestParam(name = "disc") long appId,
                                          @RequestParam(name = "article") long threadId,
                                          @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                          Model model) {
        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            model.addAttribute("username", username);

            if (app != null && applicationService.isOwnerOfApp(appId, username)) {

                maintenanceThreadViewModel.setOnEditMessage(true);

                Thread editingThread = threadService.getThread(threadId);
                if (editingThread != null) {
                    maintenanceThreadViewModel.setEditArticleId(editingThread.getId());
                    maintenanceThreadViewModel.setEditArticleSubmitter(editingThread.getSubmitter());
                    maintenanceThreadViewModel.setEditArticleEmail(editingThread.getEmail());
                    maintenanceThreadViewModel.setEditArticleSubject(editingThread.getSubject());
                    maintenanceThreadViewModel.setEditArticleCreateDt(editingThread.getCreateDt().toString());
                    maintenanceThreadViewModel.setEditArticleModDt(editingThread.getModDt().toString());
                    maintenanceThreadViewModel.setEditArticleIpAddress(editingThread.getIpAddress());
                    maintenanceThreadViewModel.setEditArticleMessage(editingThread.getBody());
                    //todo add user agent.


                }
            }
        } catch (Exception ex) {
            logger.error("Error getting edit thread view: " + ex.getMessage(), ex);
        }


        return getDiscEditView(appId, THREAD_TAB, maintenanceThreadViewModel, model);
    }

    /*
        @GetMapping("/admin/appearance-frameset.cgi")
        public ModelAndView getAppearanceFramesetView(@RequestParam(name = "id") long appId,
                                              Model model) {
            model.addAttribute("appName", "");
            model.addAttribute("appId", appId);
            return new ModelAndView("admin/appearance-frameset");
        }
    */
    @GetMapping("/admin/appearance-preview.cgi")
    public ModelAndView getAppearancePreviewView(@RequestParam(name = "id") long appId,
                                                 Model model) {
        //TODO : change this?
        return discAppController.getAppView(appId, model);
    }

    //@GetMapping("/admin/appearance-forms.cgi")
    @GetMapping("/admin/appearance-frameset.cgi")
    public ModelAndView getAppearanceView(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "redirect", required = false) String redirect,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model) {

        maintenanceViewModel.setRedirect(redirect);
        model.addAttribute("appId", appId);

        try {

            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();

            model.addAttribute("username", username);


            if (app != null && applicationService.isOwnerOfApp(appId, username)) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

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

                //threads config
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

                String nextPageButton = configurationService.getStringValue(appId, ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page");
                maintenanceViewModel.setNextPageButton(nextPageButton);

                String replyButton = configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
                maintenanceViewModel.setReplyButton(replyButton);

                //favicon config
                String favicon = configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico");
                maintenanceViewModel.setFavicon(favicon);

                //time and date config
                String timezone = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");
                maintenanceViewModel.setSelectedTimezone(timezone);

                String dateFormat = configurationService.getStringValue(appId, ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");
                maintenanceViewModel.setDateFormat(dateFormat);

                String[] timezoneIds = TimeZone.getAvailableIDs();
                List<String> timezones = Arrays.asList(timezoneIds);
                maintenanceViewModel.setTimezones(timezones);


            } else {
                maintenanceViewModel.setInfoMessage("You do not have permission to edit this disc app.");
                logger.warn("User: " + username + " has attempted to edit disc app id " + appId + ".");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }

        return new ModelAndView("admin/appearance-forms", "maintenanceViewModel", maintenanceViewModel);
    }

    @GetMapping("/admin/modify/application")
    public ModelAndView getModifyApplication(@RequestParam(name = "id") long appId,
                                             @RequestParam(name = "redirect", required = false) String redirect) {

        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/prologue-epilogue")
    public ModelAndView getModifyPrologueEpilogue(@RequestParam(name = "id") long appId,
                                                  @RequestParam(name = "redirect", required = false) String redirect) {

        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/stylesheet")
    public ModelAndView getModifyStyleSheet(@RequestParam(name = "id") long appId,
                                            @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/threads")
    public ModelAndView getModifyThreads(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/header-footer")
    public ModelAndView getModifyHeaderFooter(@RequestParam(name = "id") long appId,
                                              @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/labels")
    public ModelAndView getModifyLabels(@RequestParam(name = "id") long appId,
                                        @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/buttons")
    public ModelAndView getModifyButtons(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/favicon")
    public ModelAndView getModifyFavicon(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @GetMapping("/admin/modify/time")
    public ModelAndView getModifyTime(@RequestParam(name = "id") long appId,
                                      @RequestParam(name = "redirect", required = false) String redirect) {
        return new ModelAndView("redirect:/admin/appearance-forms.cgi?id=" + appId + "&redirect=" + redirect);
    }

    @PostMapping("/admin/modify/application")
    public ModelAndView postModifyApplication(@RequestParam(name = "id") long appId,
                                              @RequestParam(name = "redirect", required = false) String redirect,
                                              @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                              Model model) {

        if (maintenanceViewModel.getApplicationName() == null || maintenanceViewModel.getApplicationName().isEmpty()) {
            logger.warn("Cannot update application name to an empty string");
            maintenanceViewModel.setInfoMessage("Cannot update disc app name to an empty value.");
            return getAppearanceView(appId, redirect, maintenanceViewModel, model);
        }

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
            if (user != null) {
                Application app = applicationService.get(appId);
                if (app != null && app.getOwnerId().equals(user.getOwnerId())) {

                    logger.info("Updating application name of id: " + app.getId() + " from: "
                            + maintenanceViewModel.getApplicationName() + " to: " + app.getName());

                    String updatedAppName = inputHelper.sanitizeInput(maintenanceViewModel.getApplicationName());

                    app.setName(updatedAppName);
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

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);

    }

    @PostMapping("/admin/modify/stylesheet")
    public ModelAndView postModifyStyleSheet(@RequestParam(name = "id") long appId,
                                             @RequestParam(name = "redirect", required = false) String redirect,
                                             @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                             Model model) {

        if (maintenanceViewModel.getStyleSheetUrl() == null || maintenanceViewModel.getStyleSheetUrl().isEmpty()) {
            maintenanceViewModel.setInfoMessage("Style sheet URL cannot be empty. Settings not saved.");
            return getAppearanceView(appId, redirect, maintenanceViewModel, model);
        }

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);

            String styleSheetUrl = inputHelper.sanitizeInput(maintenanceViewModel.getStyleSheetUrl());

            if (!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.STYLE_SHEET_URL, styleSheetUrl)) {
                maintenanceViewModel.setInfoMessage("Failed to update Style Sheet URL.");
            } else {
                maintenanceViewModel.setInfoMessage("Successfully updated Style Sheet URL.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }

    @PostMapping("/admin/modify/prologue-epilogue")
    public ModelAndView postModifyPrologueEplilogue(@RequestParam(name = "id") long appId,
                                                    @RequestParam(name = "redirect", required = false) String redirect,
                                                    @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                                    Model model) {

        String email = accountHelper.getLoggedInEmail();

        if (email != null && !email.trim().isEmpty()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(email);
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

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);

    }


    @PostMapping("/admin/modify/threads")
    public ModelAndView postModifyThreads(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "redirect", required = false) String redirect,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model) {

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);

            if (maintenanceViewModel.getThreadSortOrder() == null || maintenanceViewModel.getThreadSortOrder().isEmpty()) {
                maintenanceViewModel.setThreadSortOrder("creation");
            }

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

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }

    @PostMapping("/admin/modify/header-footer")
    public ModelAndView postModifyHeaderFooter(@RequestParam(name = "id") long appId,
                                               @RequestParam(name = "redirect", required = false) String redirect,
                                               @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                               Model model) {

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);


            boolean headerSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.HEADER_TEXT, maintenanceViewModel.getHeader());
            boolean footerSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.FOOTER_TEXT, String.valueOf(maintenanceViewModel.getFooter()));

            if (headerSaved && footerSaved) {
                maintenanceViewModel.setInfoMessage("Successfully saved changes to header and footer.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save changes to header and footer.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }


    @PostMapping("/admin/modify/labels")
    public ModelAndView postModifyLabels(@RequestParam(name = "id") long appId,
                                         @RequestParam(name = "redirect", required = false) String redirect,
                                         @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                         Model model) {

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);


            boolean authorHeaderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SUBMITTER_LABEL_TEXT, maintenanceViewModel.getAuthorHeader());
            boolean dateHeaderSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.DATE_LABEL_TEXT, String.valueOf(maintenanceViewModel.getDateHeader()));
            boolean emailSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EMAIL_LABEL_TEXT, String.valueOf(maintenanceViewModel.getEmailHeader()));
            boolean subjectSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SUBJECT_LABEL_TEXT, String.valueOf(maintenanceViewModel.getSubjectHeader()));
            boolean messageSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.THREAD_BODY_LABEL_TEXT, String.valueOf(maintenanceViewModel.getMessageHeader()));

            if (authorHeaderSaved && dateHeaderSaved && emailSaved && subjectSaved && messageSaved) {
                maintenanceViewModel.setInfoMessage("Successfully saved changes to labels.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save changes to labels.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }


    @PostMapping("/admin/modify/buttons")
    public ModelAndView postModifyButtons(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "redirect", required = false) String redirect,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model) {

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);

            boolean shareButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.SHARE_BUTTON_TEXT, maintenanceViewModel.getShareButton());
            boolean editButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.EDIT_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getEditButton()));
            boolean returnButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReturnButton()));
            boolean previewButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.PREVIEW_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPreviewButton()));
            boolean postButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getPostButton()));
            boolean nextPageButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getNextPageButton()));
            boolean replyButtonSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, String.valueOf(maintenanceViewModel.getReplyButton()));

            if (shareButtonSaved && editButtonSaved && returnButtonSaved && previewButtonSaved && postButtonSaved
                    && nextPageButtonSaved && replyButtonSaved) {

                maintenanceViewModel.setInfoMessage("Successfully saved changes to buttons.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save changes to buttons.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }


    @PostMapping("/admin/modify/favicon")
    public ModelAndView postModifyFavicon(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "redirect", required = false) String redirect,
                                          @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                          Model model) {

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

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);

            if (!saveUpdatedConfiguration(app.getId(), ConfigurationProperty.FAVICON_URL, favicon)) {
                maintenanceViewModel.setInfoMessage("Failed to update Favicon.");
            } else {
                maintenanceViewModel.setFavicon(favicon);
                maintenanceViewModel.setInfoMessage("Successfully updated Favicon.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }

    @PostMapping("/admin/modify/time")
    public ModelAndView postModifyTime(@RequestParam(name = "id") long appId,
                                       @RequestParam(name = "redirect", required = false) String redirect,
                                       @ModelAttribute MaintenanceViewModel maintenanceViewModel,
                                       Model model) {

        if (maintenanceViewModel.getDateFormat() == null || maintenanceViewModel.getDateFormat().trim().isEmpty()) {
            maintenanceViewModel.setDateFormat("EEE MMM dd, yyyy h:mma");
        }

        String email = accountHelper.getLoggedInEmail();

        if (applicationService.isOwnerOfApp(appId, email)) {
            Application app = applicationService.get(appId);

            String dateFormat = inputHelper.sanitizeInput(maintenanceViewModel.getDateFormat());

            boolean timezoneSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.TIMEZONE_LOCATION, maintenanceViewModel.getSelectedTimezone());
            boolean dateFormatSaved = saveUpdatedConfiguration(app.getId(), ConfigurationProperty.DATE_FORMAT_PATTERN, dateFormat);

            if (timezoneSaved && dateFormatSaved) {
                logger.info("Saved date and time settings for appId: " + appId);
                maintenanceViewModel.setInfoMessage("Successfully saved changes to Date and Time.");
            } else {
                maintenanceViewModel.setInfoMessage("Failed to save changes to Date and Time.");
            }

        } else {
            maintenanceViewModel.setInfoMessage("You do not have permissions to save these changes.");
        }

        return getAppearanceView(appId, redirect, maintenanceViewModel, model);
    }


    /**
     * Saves updated or new configuration for an application
     * @param appId application id
     * @param property configuration property to save
     * @param value values to save for the configuration
     * @return returns true on success and false on failure.
     */
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


    /**
     * Generates edit thread HTML for threads view which is a stripped down version of the default view.
     * @param currentNode Node to create list HTML for
     * @param currentHtml Current html to be built upon
     * @return Returns generated HTML
     */
    private String getEditThreadHtml(ThreadTreeNode currentNode, String currentHtml) {
        currentHtml +=
                "<li>" +
                        "<a href=\"/admin/edit-thread.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                        "&amp;article=" + currentNode.getCurrent().getId() + "\">" +
                        currentNode.getCurrent().getSubject() +
                        "</a> " +

                        "<label for=\"checkbox_" + currentNode.getCurrent().getId() + "\">" +
                        "    <span style=\"font-size:smaller;\">" +
                        "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                        currentNode.getCurrent().getSubmitter() + " " +
                        currentNode.getCurrent().getCreateDt() +
                        "        </span>" +
                        "    </span>" +
                        "</label>" +
                        "<label>" +
                        "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + currentNode.getCurrent().getId() +
                        "\" id=\"checkbox_" + currentNode.getCurrent().getId() + "\"/>" +
                        "</label>" +
                        "</li>";

        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getEditThreadHtml(node, currentHtml);
            currentHtml += "</ul>";
        }

        return currentHtml;
    }

    /**
     * Gets HTML string for the edit thread view by date. Threads are sorted by date desc
     * @param threadTreeNodeList ThreadTreeNode list to use to populate HTML string
     * @return Generated HTML for node list
     */
    private String getEditThreadListHtml(List<ThreadTreeNode> threadTreeNodeList) {

        StringBuilder currentHtml = new StringBuilder("<ul>");

        List<ThreadTreeNode> allThreads = new ArrayList<>();
        for (ThreadTreeNode node : threadTreeNodeList) {
            populateFlatThreadList(node, allThreads);
        }

        //sort threads by date asc.
        allThreads.sort((node1, node2) -> {
            if (node1.getCurrent().getId().equals(node2.getCurrent().getId())) {
                return 0;
            }
            return node1.getCurrent().getId() > node2.getCurrent().getId() ? -1 : 1;
        });


        for (ThreadTreeNode currentNode : allThreads) {
            currentHtml.append(
                    "<li>" +
                            "<a href=\"/admin/edit-thread.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() + "\">" +
                            currentNode.getCurrent().getSubject() +
                            "</a> " +

                            "<label for=\"checkbox_" + currentNode.getCurrent().getId() + "\">" +
                            "    <span style=\"font-size:smaller;\">" +
                            "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                            currentNode.getCurrent().getSubmitter() + " " +
                            currentNode.getCurrent().getCreateDt() +
                            "        </span>" +
                            "    </span>" +
                            "</label>" +
                            "<label>" +
                            "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + currentNode.getCurrent().getId() +
                            "\" id=\"checkbox_" + currentNode.getCurrent().getId() + "\" \"/>" +
                            "</label>" +
                            "</li>"
            );

        }
        currentHtml.append("</ul>");
        return currentHtml.toString();
    }

    /**
     * Populates the threadTreeNodeList with all of the sub threads passed into the current parameter as
     * a flat list.
     * @param current current ThreadTreeNode to start populating from (will traverse sub nodes)
     * @param threadTreeNodeList ThreadTreeNode list to add parent and sub nodes too from current node.
     */
    private void populateFlatThreadList(ThreadTreeNode current, List<ThreadTreeNode> threadTreeNodeList) {
        threadTreeNodeList.add(current);
        for (ThreadTreeNode threadTreeNode : current.getSubThreads()) {
            populateFlatThreadList(threadTreeNode, threadTreeNodeList);
        }
    }


    private String getSearchThreadHtml(List<Thread> threads) {
        String currentHtml = "<ul>";

        for (Thread thread : threads) {

            currentHtml += "<li>" +
                    "<a href=\"/admin/edit-thread.cgi?disc=" + thread.getApplicationId() +
                    "&amp;article=" + thread.getId() + "\">" +
                    thread.getSubject() +
                    "</a> " +

                    "<label for=\"checkbox_" + thread.getId() + "\">" +
                    "    <span style=\"font-size:smaller;\">" +
                    "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                    thread.getSubmitter() + " " +
                    thread.getCreateDt() +
                    "        </span>" +
                    "    </span>" +
                    "</label>" +
                    "<label>" +
                    "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + thread.getId() +
                    "\" id=\"checkbox_" + thread.getId() + "\" \"/>" +
                    "</label>" +
                    "</li>";
        }
        currentHtml += "</ul>";
        return currentHtml;
    }
}
