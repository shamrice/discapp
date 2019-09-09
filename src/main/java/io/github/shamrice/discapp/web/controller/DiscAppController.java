package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.stats.StatisticsService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.NewThreadViewModel;
import io.github.shamrice.discapp.web.model.ThreadViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@Slf4j
public class DiscAppController {

    private static final String BLANK_SUBMITTER = "Anonymous";
    private static final String BLANK_SUBJECT = "No Subject";
    private static final String BLANK_REPLY_PREFIX = "Re: ";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private ErrorController errorController;

    @GetMapping("/Indices/{applicationId}.html")
    public ModelAndView getAppViewOriginalUrl(@PathVariable(name = "applicationId") Long appId,
                                              @RequestParam(name = "page", required = false) Integer page,
                                              Model model,
                                              HttpServletRequest request) {
        return getAppView(appId, page, model, request);
    }

    @GetMapping("/indices/{applicationId}")
    public ModelAndView getAppView(@PathVariable(name = "applicationId") Long appId,
                                   @RequestParam(name = "page", required = false) Integer page,
                                   Model model,
                                   HttpServletRequest request) {

        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                model.addAttribute("prologueText", applicationService.getPrologueText(app.getId()));
                model.addAttribute("epilogueText", applicationService.getEpilogueText(app.getId()));

                model.addAttribute("postMessageButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));
                model.addAttribute("nextPageButtonText", configurationService.getStringValue(appId, ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page"));

                if (page == null) {
                    page = 0;
                }
                model.addAttribute("currentPage", page);
                model.addAttribute("nextPage", page + 1);
                model.addAttribute("hasNextPage", true); // default.

                //get ip address
                if (request != null) {
                    //check forwarded header for proxy users, if not found, use ip provided.
                    String ipAddress = request.getHeader("X-FORWARDED-FOR");
                    if (ipAddress == null || ipAddress.isEmpty()) {
                        ipAddress = request.getRemoteAddr();
                    }
                    statisticsService.increaseCurrentPageStats(app.getId(), ipAddress);
                }

                //get threads
                int maxThreads = configurationService.getIntegerValue(appId, ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, 25);
                boolean showTopLevelPreview = configurationService.getBooleanValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, true);
                boolean isExpandOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, false);
                String threadSortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.CREATION.name());

                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), page, maxThreads, ThreadSortOrder.valueOf(threadSortOrder.toUpperCase()));

                //if there are less than max threads returned, we must be on the last page.
                if (threadTreeNodeList.size() < maxThreads) {
                    model.addAttribute("hasNextPage", false);
                }

                if (isExpandOnIndex) {

                    List<String> threadTreeHtml = new ArrayList<>();
                    String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");

                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                        String currentHtml = getAppViewTopThreadHtml(threadTreeNode, entryBreakString, showTopLevelPreview, page);

                        //get replies if they exist and add on HTML.
                        if (threadTreeNode.getSubThreads() != null && threadTreeNode.getSubThreads().size() > 0) {
                            currentHtml += "<div class=\"responses\">";
                            currentHtml += getAppViewThreadHtml(threadTreeNode, "",
                                    entryBreakString, true, -1,
                                    false, page);
                            currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
                            currentHtml += "</div>";
                        }

                        threadTreeHtml.add(currentHtml);
                    }

                    model.addAttribute("threadNodeList", threadTreeHtml);
                } else {
                    model.addAttribute("dateLabel", configurationService.getStringValue(appId, ConfigurationProperty.DATE_LABEL_TEXT, "Date:"));
                    model.addAttribute("submitterLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
                    model.addAttribute("subjectLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));

                    List<ThreadViewModel> threads = new ArrayList<>();
                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                        ThreadViewModel threadViewModel = new ThreadViewModel();

                        threadViewModel.setSubmitter(threadTreeNode.getCurrent().getSubmitter());
                        threadViewModel.setSubject(threadTreeNode.getCurrent().getSubject());
                        threadViewModel.setCreateDt(getAdjustedDateStringForConfiguredTimeZone(appId, threadTreeNode.getCurrent().getCreateDt(), false));
                        threadViewModel.setId(threadTreeNode.getCurrent().getId().toString());
                        threadViewModel.setShowMoreOnPreviewText(false);

                        String body = threadTreeNode.getCurrent().getBody();
                        String previewText = null;
                        if (body != null && !body.isEmpty()) {
                            if (body.length() > 320) { //todo : system configuration for length.
                                previewText = inputHelper.sanitizeInput(body.substring(0, 320));
                                previewText += "...";
                                threadViewModel.setShowMoreOnPreviewText(true);
                            } else {
                                previewText = inputHelper.sanitizeInput(body);
                            }
                        }
                        threadViewModel.setPreviewText(previewText);

                        threads.add(threadViewModel);

                    }
                    model.addAttribute("threads", threads);

                }

                model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute("threadSeparator", configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));
                model.addAttribute("isExpandOnIndex", isExpandOnIndex);
                model.addAttribute("isShowTopLevelPreview", showTopLevelPreview);

                return new ModelAndView("indices/appView");

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
                log.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            log.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return errorController.getNotFoundView("Disc App with ID of " + appId + " does not exist.", model);
        //return new ModelAndView("redirect:/error/notfound", "errorText", "Disc App with ID of " + appId + " does not exist.");
    }

    @GetMapping("/createThread")
    public ModelAndView getCreateNewThreadRedirect(@RequestParam(name = "disc") Long appId, Model model) {
        log.debug("Attempted GET on create thread page. Redirecting to main view for appId: " + appId);
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @PostMapping("/createThread")
    public ModelAndView createNewThread(@RequestParam(name = "disc") Long appId,
                                        @ModelAttribute ThreadViewModel threadViewModel,
                                        @ModelAttribute NewThreadViewModel newThreadViewModel,
                                        Model model) {
        Application app = applicationService.get(appId);

        long parentId = 0L;

        //if coming from view page
        if (threadViewModel != null && threadViewModel.getId() != null) {
            try {
                parentId = Long.parseLong(threadViewModel.getId());
                model.addAttribute("parentThreadSubmitter", threadViewModel.getSubmitter());
                model.addAttribute("parentThreadSubject", threadViewModel.getSubject());
                model.addAttribute("parentThreadBody", threadViewModel.getBody());
                model.addAttribute("currentPage", threadViewModel.getCurrentPage());
            } catch (NumberFormatException ex) {
                log.error("Unable to parse parent id from view thread model. appId: " + appId
                        + " : attempted parentId: " + threadViewModel.getId());
            }
        }

        //if coming from preview page
        if (newThreadViewModel != null) {
            model.addAttribute("submitter", newThreadViewModel.getSubmitter());
            model.addAttribute("subject", newThreadViewModel.getSubject());
            model.addAttribute("email", newThreadViewModel.getEmail());
            model.addAttribute("body", newThreadViewModel.getBody());
            model.addAttribute("showEmail", newThreadViewModel.isShowEmail());
            model.addAttribute("parentThreadSubmitter", newThreadViewModel.getParentThreadSubmitter());
            model.addAttribute("parentThreadSubject", newThreadViewModel.getParentThreadSubject());
            model.addAttribute("parentThreadBody", newThreadViewModel.getParentThreadBody());
            model.addAttribute("currentPage", newThreadViewModel.getCurrentPage());

            if (newThreadViewModel.getParentId() != null) {
                try {
                    parentId = Long.parseLong(newThreadViewModel.getParentId());
                } catch (NumberFormatException ex) {
                    parentId = 0L;
                    log.error("Unable to parse parent id from returned new thread model from preview page. appId: " + appId
                            + " : attempted parentId: " + newThreadViewModel.getParentId());
                }
            }
        }

        //pre-fill form with user info if they are logged in
        if (accountHelper.isLoggedIn()) {
            model.addAttribute("isLoggedIn", "true");

            DiscAppUser user = discAppUserDetailsService.getByEmail(accountHelper.getLoggedInEmail());
            model.addAttribute("submitter", user.getUsername());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("showEmail", user.getShowEmail());
        }

        model.addAttribute("appName", app.getName());
        model.addAttribute("appId", appId);
        model.addAttribute("parentThreadId", parentId); //parentThreadId);

        model.addAttribute("submitterLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
        model.addAttribute("emailLabel", configurationService.getStringValue(appId, ConfigurationProperty.EMAIL_LABEL_TEXT, "Email:"));
        model.addAttribute("subjectLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));
        model.addAttribute("bodyLabel", configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message Text:"));
        model.addAttribute("previewButtonText", configurationService.getStringValue(appId, ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview"));
        model.addAttribute("postButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));
        model.addAttribute("returnButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages"));
        model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

        model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
        model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

        return new ModelAndView("indices/createThread");
    }

    @PostMapping("/previewThread")
    public ModelAndView postPreviewThread(@RequestParam(name = "disc") Long appId,
                             NewThreadViewModel newThreadViewModel,
                             Model model) {

        Application app = applicationService.get(appId);
        model.addAttribute("appName", app.getName());
        model.addAttribute("appId", appId);
        model.addAttribute("newThreadViewModel", newThreadViewModel);

        String htmlBody = newThreadViewModel.getBody();
        if (htmlBody != null && !htmlBody.isEmpty()) {
            htmlBody = htmlBody.replaceAll("\r", "<br />");
        }
        newThreadViewModel.setHtmlBody(htmlBody);


        model.addAttribute("editButtonText", configurationService.getStringValue(appId, ConfigurationProperty.EDIT_BUTTON_TEXT, "Edit Message"));
        model.addAttribute("postButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));
        model.addAttribute("returnButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages"));
        model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

        model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
        model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

        return new ModelAndView("indices/previewThread", "model", model);
    }

    @PostMapping("/postThread")
    public ModelAndView postNewThread(@RequestParam(name = "disc") Long appId,
                                      @ModelAttribute NewThreadViewModel newThreadViewModel,
                                      ThreadViewModel threadViewModel,
                                      Model model,
                                      HttpServletRequest request) {
        if (newThreadViewModel != null) {

            if (newThreadViewModel.getReturnToApp() != null && !newThreadViewModel.getReturnToApp().isEmpty()) {
                log.info("Return to app button clicked for app id " + appId + ". Value=" + newThreadViewModel.getReturnToApp());

                if (newThreadViewModel.getCurrentPage() != null && newThreadViewModel.getCurrentPage() > 0) {
                    int page = newThreadViewModel.getCurrentPage();
                    return new ModelAndView("redirect:/indices/" + appId + "?page=" + page);
                }
                return new ModelAndView("redirect:/indices/" + appId);

            } else if (newThreadViewModel.getPreviewArticle() != null && !newThreadViewModel.getPreviewArticle().isEmpty()) {
                return postPreviewThread(appId, newThreadViewModel, model);

            } else if (newThreadViewModel.getSubmitNewThread() != null && !newThreadViewModel.getSubmitNewThread().isEmpty()) {

                int page = 0;
                if (newThreadViewModel.getCurrentPage() != null && newThreadViewModel.getCurrentPage() > 0) {
                    page = newThreadViewModel.getCurrentPage();
                }

                if ((newThreadViewModel.getSubject() == null || newThreadViewModel.getSubject().trim().isEmpty())
                        && (newThreadViewModel.getBody() == null || newThreadViewModel.getBody().trim().isEmpty())) {
                    log.warn("AppId: " + appId + " cannot create new thread with no subject and no body.");
                    return new ModelAndView("redirect:/indices/" + appId + "?page=" + page);
                }

                //set submitter to anon if not filled out
                String submitter = "";
                if (newThreadViewModel.getSubmitter() == null || newThreadViewModel.getSubmitter().trim().isEmpty()) {
                    submitter = BLANK_SUBMITTER;
                } else {
                    submitter = newThreadViewModel.getSubmitter();
                }

                long parentId = Long.parseLong(newThreadViewModel.getParentId());

                //set subject if blank depending if new thread or a reply and has message body.
                String subject = "";
                if ((newThreadViewModel.getSubject() == null || newThreadViewModel.getSubject().trim().isEmpty())) {
                    if (parentId == 0L) {
                        subject = BLANK_SUBJECT;
                    } else {
                        subject = BLANK_REPLY_PREFIX + newThreadViewModel.getParentThreadSubject();
                    }
                } else {
                    subject = newThreadViewModel.getSubject();
                }

                //sanitize inputs.
                subject = inputHelper.sanitizeInput(subject);
                submitter = inputHelper.sanitizeInput(submitter);
                String email = inputHelper.sanitizeInput(newThreadViewModel.getEmail());

                log.info("new thread: " + newThreadViewModel.getAppId() + " : " + submitter + " : "
                        + subject + " : " + newThreadViewModel.getBody());

                Thread newThread = new Thread();
                newThread.setApplicationId(appId);
                newThread.setParentId(parentId);
                newThread.setDeleted(false);
                newThread.setCreateDt(new Date());
                newThread.setModDt(new Date());
                newThread.setSubject(subject);

                //set values for logged in user, if not logged in... use form data.
                String userEmail = accountHelper.getLoggedInEmail();

                DiscAppUser discAppUser = discAppUserDetailsService.getByEmail(userEmail);
                if (discAppUser != null) {
                    newThread.setDiscAppUser(discAppUser);
                    newThread.setSubmitter(discAppUser.getUsername());
                    newThread.setEmail(discAppUser.getEmail());
                    newThread.setShowEmail(discAppUser.getShowEmail());

                } else {
                    newThread.setSubmitter(submitter);
                    newThread.setEmail(email);

                    //only use input from checkbox if there's an email address entered
                    newThread.setShowEmail(!email.isEmpty() && newThreadViewModel.isShowEmail());
                }

                //set ip address
                if (request != null) {
                    //check forwarded header for proxy users, if not found, use ip provided.
                    String ipAddress = request.getHeader("X-FORWARDED-FOR");
                    if (ipAddress == null || ipAddress.isEmpty()) {
                        ipAddress = request.getRemoteAddr();
                    }
                    newThread.setIpAddress(ipAddress);

                    String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
                    if (userAgent != null && !userAgent.isEmpty()) {
                        newThread.setUserAgent(userAgent);
                    }
                }

                String body = newThreadViewModel.getBody();
                if (body != null && !body.trim().isEmpty()) {
                    body = body.replaceAll("\r", "<br />");

                    body = inputHelper.addUrlHtmlLinksToString(body);
                }

                threadService.saveThread(newThread, body);
                return new ModelAndView("redirect:/indices/" + appId + "?page=" + page);
            }
        }
        log.info("Error posting thread or couldn't find redirect action for POST. Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @GetMapping("discussion.cgi")
    public String getViewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "article", required = false) Long threadId,
                                @RequestParam(name = "page", required = false) Integer currentPage,
                                Model model) {

        if (threadId == null || threadId < 1) {
            log.error("Null or invalid article id passed to view thread. Returning to app view for appId: " + appId);
            return "redirect:/indices/" + appId;
        }

        if (currentPage == null || currentPage < 0) {
            currentPage = 0;
        }

        log.info("Getting thread id " + threadId + " for app id: " + appId);
        ThreadViewModel threadViewModel = new ThreadViewModel();

        Thread currentThread = threadService.getThread(appId, threadId);
        if (currentThread != null) {

            String threadBody = threadService.getThreadBodyText(threadId);
            String subThreadsHtml = getThreadViewThreadHtml(currentThread, currentPage);

            DiscAppUser sourceUser = currentThread.getDiscAppUser();

            //set current username if exists and different than in thread table.
            if (sourceUser != null && !currentThread.getSubmitter().equals(sourceUser.getUsername())) {
                threadViewModel.setCurrentUsername(sourceUser.getUsername());
            }

            threadViewModel.setCurrentPage(currentPage);
            threadViewModel.setBody(threadBody);
            threadViewModel.setModDt(currentThread.getModDt().toString());
            threadViewModel.setAppId(appId.toString());
            threadViewModel.setId(threadId.toString());
            threadViewModel.setIpAddress(currentThread.getIpAddress());
            threadViewModel.setParentId(currentThread.getParentId().toString());
            threadViewModel.setSubject(currentThread.getSubject());
            threadViewModel.setSubmitter(currentThread.getSubmitter());

            //use source user info if exists first.
            if (sourceUser != null && sourceUser.getEmail() != null) {
                threadViewModel.setEmail(sourceUser.getEmail());
                threadViewModel.setShowEmail(sourceUser.getShowEmail());
            } else if (currentThread.getEmail() != null && !currentThread.getEmail().isEmpty()) {
                threadViewModel.setEmail(currentThread.getEmail());
                threadViewModel.setShowEmail(currentThread.isShowEmail());
            } else {
                //don't attempt to show a null or empty email regardless what was selected.
                threadViewModel.setShowEmail(false);
            }

            //adjust date in view to current timezone and proper formatting
            threadViewModel.setCreateDt(getAdjustedDateStringForConfiguredTimeZone(appId, currentThread.getCreateDt(), true));

            Application app = applicationService.get(appId);
            model.addAttribute("appName", app.getName());

            model.addAttribute("threadViewModel", threadViewModel);
            model.addAttribute("subThreadsHtml", subThreadsHtml);

            model.addAttribute("replyButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply"));
            model.addAttribute("returnButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages"));
            model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
            model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

            model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
            model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
        } else {
            log.warn("Attempted to view thread: " + threadId + " on appId: " + appId + " but does not belong to app. Redirecting to appView.");
            return "redirect:/indices/" + appId;
        }

        return "indices/viewThread";
    }

    @PostMapping("discussion.cgi")
    public ModelAndView postDiscussionForm(@RequestParam(name = "disc") Long appId,
                                           ThreadViewModel threadViewModel,
                                           Model model) {
        if (threadViewModel != null) {
            if (threadViewModel.getReturnToApp() != null && !threadViewModel.getReturnToApp().isEmpty()) {
                int currentPage = 0;
                if (threadViewModel.getCurrentPage() != null && threadViewModel.getCurrentPage() > 0) {
                    currentPage = threadViewModel.getCurrentPage();
                }

                log.info("Return to app button clicked for app id " + appId + ". Value=" + threadViewModel.getReturnToApp());
                return new ModelAndView("redirect:/indices/" + appId + "?page=" + currentPage + "#" + threadViewModel.getId());

            } else if (threadViewModel.getPostResponse() != null && !threadViewModel.getPostResponse().isEmpty()) {

                log.info("new reply appId: " + threadViewModel.getAppId() + " parent id : " + threadViewModel.getId()
                        + " submitter: " + threadViewModel.getSubmitter() + " : subject: "
                        + threadViewModel.getSubject() + " : email: " + threadViewModel.getEmail()
                        + " : body: " + threadViewModel.getBody());

                return createNewThread(appId, threadViewModel, null, model);
            }
        }

        log.info("Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @PostMapping("/indices/search")
    public ModelAndView searchDiscApp(@RequestParam(name = "disc") Long appId,
                                      @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                      @RequestParam(name = "returnToApp", required = false) String returnToApp,
                                      Model model) {

        if (searchTerm == null || searchTerm.isEmpty() || returnToApp != null) {
            log.info("Empty search term entered for appId: " + appId + " : returning to app view page.");
            return new ModelAndView("redirect:/indices/" + appId);
        }

        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                //get search results
                List<Thread> foundThreads = threadService.searchThreads(appId, searchTerm);
                String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                model.addAttribute("searchResults", getSearchThreadHtml(foundThreads, entryBreakString));
                model.addAttribute("searchTerm", searchTerm);

                model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute("threadSeparator", configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

                return new ModelAndView("indices/search", "model", model);

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
                log.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            log.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return new ModelAndView("redirect:/indices/" + appId);
    }

    private String getSearchThreadHtml(List<Thread> threads, String entryBreakString) {
        String currentHtml = "<ul>";

        for (Thread thread : threads) {

            currentHtml += " <li>"
                    + "        <a "
                    + " href=\"/discussion.cgi?disc=" + thread.getApplicationId()
                    + "&amp;article=" + thread.getId() + "\""
                    + " name=\"" + thread.getId() + "\">"
                    + thread.getSubject()
                    + "</a>  "
                    + "        <span class=\"author\"> " + thread.getSubmitter() + "</span> "
                    + "        <span class=\"date\"> " +
                            getAdjustedDateStringForConfiguredTimeZone(
                                thread.getApplicationId(),
                                thread.getCreateDt(), true) +
                    "</span> "
                    + "</li>";
        }

        currentHtml += "</ul>";
        return currentHtml;
    }

    private String getAppViewTopThreadHtml(ThreadTreeNode currentNode, String entryBreakString,
                                           boolean showPreviewText, int currentPage) {

        String messageDivText = "first_message_div";
        String messageHeaderText = "first_message_header";
        String messageSpanText = "first_message_span";

        if (isNewMessageHighlighted(currentNode)) {
            messageSpanText += " new_message";
        }

        String topThreadHtml = "" +
                "        <div class=\"" + messageDivText + "\">" +
                "            <div class=\"" + messageHeaderText + "\">" +
                "               <span class=\"" + messageSpanText + "\">" +
                "                   <a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                "&amp;article=" + currentNode.getCurrent().getId() +
                "&amp;page=" + currentPage + "\"" +
                " name=\"" + currentNode.getCurrent().getId() + "\">" +
                currentNode.getCurrent().getSubject() +
                "                   </a> " + entryBreakString +
                "                   <span class=\"author_cell\">" + currentNode.getCurrent().getSubmitter() + ",</span> " +
                "                   <span class=\"date_cell\">" +
                getAdjustedDateStringForConfiguredTimeZone(
                        currentNode.getCurrent().getApplicationId(),
                        currentNode.getCurrent().getCreateDt(),
                        false) +
                "                   </span>" +
                "               </span>" +
                "            </div>" +
                "        </div>";

        if (showPreviewText) {
            String previewText = currentNode.getCurrent().getBody();
            if (previewText != null && !previewText.isEmpty()) {

                previewText = inputHelper.sanitizeInput(previewText); //remove html from thread preview

                if (previewText.length() > 320) {
                    previewText = previewText.substring(0, 320); //todo configurable length
                    previewText +=  "...<a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() +
                            "&amp;page=" + currentPage + "\"" +
                            " name=\"" + currentNode.getCurrent().getId() + "\">" +
                            " more</a> ";
                }
                topThreadHtml += "<div class=\"first_message\">" + previewText + "</div>";
            }
        }

        return topThreadHtml;
    }

    /**
     * Recursive function that builds HTML for each thread in the app.
     * @param currentNode Current node being built. When calling, top level node is passed.
     * @param currentHtml Current html string. This is the the string that is built and returned
     * @return built HTML list structure for thread
     */
    private String getAppViewThreadHtml(ThreadTreeNode currentNode, String currentHtml, String entryBreakString,
                                        boolean skipCurrentNode, long currentlyViewedId,
                                        boolean showPreviewText, int currentPage) {

        if (!skipCurrentNode) {

            //current entry gets different css and no inner div.
            if (currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += "<li class=\"current_entry\">";
            } else {
                currentHtml += "<li class=\"message_entry\">" +
                        " <div class=\"response_headers\">";
            }

            if (isNewMessageHighlighted(currentNode)) {
                currentHtml += "   <span class=\"response_headers new_message\">";
            } else if (!isNewMessageHighlighted(currentNode) && currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                //if current thread, apply different css class to span
                currentHtml += "   <span class=\"current_entry\">";
            } else {
                currentHtml += "   <span class=\"response_headers\">";
            }

            //if rendering current thread, subject line should not have an anchor tag.
            if (currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += currentNode.getCurrent().getSubject();
            } else {
                currentHtml += "      <a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                        "&amp;article=" + currentNode.getCurrent().getId() +
                        "&amp;page=" + currentPage + "\"" +
                        " name=\"" + currentNode.getCurrent().getId() + "\">" +
                        currentNode.getCurrent().getSubject() +
                        "</a> ";
            }

            currentHtml += entryBreakString +
                    "      <span class=\"author_cell\">" + currentNode.getCurrent().getSubmitter() + ",</span> " +
                    "      <span class=\"date_cell\">" +
                    getAdjustedDateStringForConfiguredTimeZone(
                            currentNode.getCurrent().getApplicationId(),
                            currentNode.getCurrent().getCreateDt(),
                            false) +
                    "       </span>" +
                    "   </span>";
            //close div tag on non-current entries.
            if (!currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += "</div>";
            }

            //only show preview if selected and not currently viewed thread
            if (showPreviewText && !currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                String previewText = currentNode.getCurrent().getBody();
                if (previewText != null && !previewText.isEmpty()) {

                    previewText = inputHelper.sanitizeInput(previewText); //clear HTML from preview thread body

                    if (previewText.length() > 200) {
                        previewText = previewText.substring(0, 200); //todo configurable length
                        previewText +=  "...<a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                                "&amp;article=" + currentNode.getCurrent().getId() +
                                "&amp;page=" + currentPage + "\"" +
                                " name=\"" + currentNode.getCurrent().getId() + "\">" +
                                " more</a> ";
                    }
                    currentHtml += "<div class=\"message_preview\">" + previewText + "</div>";
                }
            }

            currentHtml += "</li>";
        }

        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {

            currentHtml += "<li class=\"nested_list\"><ul>";
            currentHtml = getAppViewThreadHtml(node, currentHtml, entryBreakString, false,
                    currentlyViewedId, showPreviewText, currentPage);
            currentHtml += "</li>";
        }

        currentHtml += "</ul>";

        return currentHtml;
    }


    /**
     * Generates the thread view reply thread tree HTML by setting up the grandparent id and calling the
     * getAppViewThreadHtml method. Todo: It's a mess that will later need refactoring...
     * @param currentThread Current thread being viewed on the view thread page.
     * @return Returns formatted HTML block for reply threads.
     */
    private String getThreadViewThreadHtml(Thread currentThread, int currentPage) {

        //default reply thread values
        boolean skipCurrent = true;
        boolean isFirstChild = false;
        String currentHtml = "";
        Long grandparentId = 0L;

        //if not top level thread:
        if (currentThread.getParentId() != null && currentThread.getParentId() != 0L) {

            Thread parentThread = threadService.getThread(currentThread.getApplicationId(), currentThread.getParentId());
            if (parentThread != null) {

                //if grandparent is not top level thread.
                if (parentThread.getParentId() != 0L) {
                    grandparentId = parentThread.getParentId();
                } else {
                    //if grandparent is top level thread... set to parent
                    grandparentId = parentThread.getId();
                    skipCurrent = false;
                    isFirstChild = true;

                }
            } else {
                grandparentId = currentThread.getId();
            }
        }

        //if viewing top level thread, replies are formatted slightly differently.
        if (grandparentId == null || grandparentId == 0L) {
            grandparentId = currentThread.getId();
        }

        //get thread tree starting at grandparent id determined above
        ThreadTreeNode subThreadNode = threadService.getFullThreadTree(grandparentId);

        if (subThreadNode != null) {
            String entryBreakString = configurationService.getStringValue(currentThread.getApplicationId(),
                    ConfigurationProperty.ENTRY_BREAK_TEXT, "-");

            currentHtml += getAppViewThreadHtml(subThreadNode, currentHtml, entryBreakString,
                    skipCurrent, currentThread.getId(), true, currentPage) ;

            //first child thread needs additional html tags added
            if (isFirstChild) {
                currentHtml = "<div class=\"nested_list\"><ul>" + currentHtml + "</div>";
            } else {
                currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
            }
        }

        return currentHtml;
    }

    private String getAdjustedDateStringForConfiguredTimeZone(long appId, Date date, boolean includeComma) {

        String dateFormatPattern = configurationService.getStringValue(appId, ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");
        String timeZoneLocation = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");

        DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneLocation));

        //am and pm should be lowercase.
        String formattedString = dateFormat.format(date).replace("AM", "am").replace("PM", "pm");

        if (!includeComma) {
            formattedString = formattedString.replace(",", "");
        }

        return formattedString;
    }

    private boolean isNewMessageHighlighted(ThreadTreeNode currentNode) {

        if (currentNode != null && currentNode.getCurrent() != null && currentNode.getCurrent().getCreateDt() != null) {

            boolean highlightNewMessages = configurationService.getBooleanValue(
                    currentNode.getCurrent().getApplicationId(),
                    ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, false);

            Instant now = Instant.now();
            Instant currentNodeInstant = currentNode.getCurrent().getCreateDt().toInstant();

            return highlightNewMessages && currentNodeInstant.isAfter(now.minus(24, ChronoUnit.HOURS));
        }

        log.warn("Null thread node or create date sent to be checked if highlight functionality should be applied.");
        return false;
    }
}
