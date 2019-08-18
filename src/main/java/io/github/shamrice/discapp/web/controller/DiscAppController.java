package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.NewThreadViewModel;
import io.github.shamrice.discapp.web.model.ThreadViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class DiscAppController {

    private static final Logger logger = LoggerFactory.getLogger(DiscAppController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @GetMapping("/indices/{applicationId}")
    public ModelAndView getAppView(@PathVariable(name = "applicationId") Long appId, Model model) {

        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                model.addAttribute("prologueText", applicationService.getPrologueText(app.getId()));
                model.addAttribute("epilogueText", applicationService.getEpilogueText(app.getId()));

                model.addAttribute("postMessageButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));

                //get threads
                int maxThreads = configurationService.getIntegerValue(appId, ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, 25);
                boolean showTopLevelPreview = configurationService.getBooleanValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, true);

                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), maxThreads);
                List<String> threadTreeHtml = new ArrayList<>();
                String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");

                for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                    String currentHtml = getAppViewTopThreadHtml(threadTreeNode, entryBreakString, showTopLevelPreview);

                    //get replies if they exist and add on HTML.
                    if (threadTreeNode.getSubThreads() != null && threadTreeNode.getSubThreads().size() > 0) {
                        currentHtml += "<div class=\"responses\">";
                        currentHtml += getAppViewThreadHtml(threadTreeNode, "", entryBreakString, true, -1, false);
                        currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
                        currentHtml += "</div>";
                    }

                    threadTreeHtml.add(currentHtml);
                }

                model.addAttribute("threadNodeList", threadTreeHtml);
                model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute("threadSeparator", configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute("faviconUrl", configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute("styleSheetUrl", configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));

                return new ModelAndView("indices/appView");

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
                logger.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            logger.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return new ModelAndView("redirect:/error/notfound", "errorText", "Disc App with ID of " + appId + " does not exist.");
    }

    @GetMapping("/createThread")
    public ModelAndView getCreateNewThreadRedirect(@RequestParam(name = "disc") Long appId, Model model) {
        logger.debug("Attempted GET on create thread page. Redirecting to main view for appId: " + appId);
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
            } catch (NumberFormatException ex) {
                logger.error("Unable to parse parent id from view thread model. appId: " + appId
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

            if (newThreadViewModel.getParentId() != null) {
                try {
                    parentId = Long.parseLong(newThreadViewModel.getParentId());
                } catch (NumberFormatException ex) {
                    parentId = 0L;
                    logger.error("Unable to parse parent id from returned new thread model from preview page. appId: " + appId
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
                logger.info("Return to app button clicked for app id " + appId + ". Value=" + newThreadViewModel.getReturnToApp());

                return new ModelAndView("redirect:/indices/" + appId);

            } else if (newThreadViewModel.getPreviewArticle() != null && !newThreadViewModel.getPreviewArticle().isEmpty()) {

                return postPreviewThread(appId, newThreadViewModel, model);

            } else if (newThreadViewModel.getSubmitNewThread() != null && !newThreadViewModel.getSubmitNewThread().isEmpty()
                    && newThreadViewModel.getSubmitter() != null && !newThreadViewModel.getSubmitter().isEmpty()
                    && newThreadViewModel.getSubject() != null && !newThreadViewModel.getSubject().isEmpty()) {

                logger.info("new thread: " + newThreadViewModel.getAppId() + " : " + newThreadViewModel.getSubmitter() + " : "
                        + newThreadViewModel.getSubject() + " : " + newThreadViewModel.getBody());

                String subject = inputHelper.sanitizeInput(newThreadViewModel.getSubject());
                String submitter = inputHelper.sanitizeInput(newThreadViewModel.getSubmitter());
                String email = inputHelper.sanitizeInput(newThreadViewModel.getEmail());

                Thread newThread = new Thread();
                newThread.setApplicationId(appId);
                newThread.setParentId(Long.parseLong(newThreadViewModel.getParentId()));
                newThread.setDeleted(false);
                newThread.setCreateDt(new Date());
                newThread.setModDt(new Date());
                newThread.setSubject(subject);


                //set values for logged in user, if not logged in... use form data.
                String userEmail = accountHelper.getLoggedInEmail();

                DiscAppUser discAppUser = discAppUserDetailsService.getByEmail(userEmail);
                if (discAppUser != null) {
                    newThread.setDiscappUserId(discAppUser.getId());
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
                }

                String body = newThreadViewModel.getBody();
                if (body != null && !body.isEmpty()) {
                    body = body.replaceAll("\r", "<br />");

                    body = inputHelper.addUrlHtmlLinksToString(body);
                }

                threadService.saveThread(newThread, body);
            }
        }
        logger.info("Error posting thread or couldn't find redirect action for POST. Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @GetMapping("discussion.cgi")
    public String getViewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "article", required = false) Long threadId,
                                Model model) {

        if (threadId == null || threadId < 1) {
            logger.error("Null or invalid article id passed to view thread. Returning to app view for appId: " + appId);
            return "redirect:/indices/" + appId;
        }

        logger.info("Getting thread id " + threadId + " for app id: " + appId);

        Thread currentThread = threadService.getThread(threadId);
        if (currentThread != null) {

            String threadBody = threadService.getThreadBodyText(threadId);
            String subThreadsHtml = getThreadViewThreadHtml(currentThread);

            ThreadViewModel threadViewModel = new ThreadViewModel();
            threadViewModel.setBody(threadBody);
            threadViewModel.setModDt(currentThread.getModDt().toString());
            threadViewModel.setAppId(appId.toString());
            threadViewModel.setId(threadId.toString());
            threadViewModel.setIpAddress(currentThread.getIpAddress());
            threadViewModel.setParentId(currentThread.getParentId().toString());
            threadViewModel.setSubject(currentThread.getSubject());
            threadViewModel.setSubmitter(currentThread.getSubmitter());
            if (currentThread.getEmail() != null && !currentThread.getEmail().isEmpty()) {
                threadViewModel.setEmail(currentThread.getEmail());
                threadViewModel.setShowEmail(currentThread.getShowEmail());
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
        }

        return "indices/viewThread";
    }

    @PostMapping("discussion.cgi")
    public ModelAndView postDiscussionForm(@RequestParam(name = "disc") Long appId,
                                           ThreadViewModel threadViewModel,
                                           Model model) {
        if (threadViewModel != null) {
            if (threadViewModel.getReturnToApp() != null && !threadViewModel.getReturnToApp().isEmpty()) {

                logger.info("Return to app button clicked for app id " + appId + ". Value=" + threadViewModel.getReturnToApp());
                return new ModelAndView("redirect:/indices/" + appId + "#" + threadViewModel.getId());

            } else if (threadViewModel.getPostResponse() != null && !threadViewModel.getPostResponse().isEmpty()) {

                logger.info("new reply appId: " + threadViewModel.getAppId() + " parent id : " + threadViewModel.getId()
                        + " submitter: " + threadViewModel.getSubmitter() + " : subject: "
                        + threadViewModel.getSubject() + " : email: " + threadViewModel.getEmail()
                        + " : body: " + threadViewModel.getBody());

                return createNewThread(appId, threadViewModel, null, model);
            }
        }

        logger.info("Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @PostMapping("/indices/search")
    public ModelAndView searchDiscApp(@RequestParam(name = "disc") Long appId,
                                      @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                      @RequestParam(name = "returnToApp", required = false) String returnToApp,
                                      Model model) {

        if (searchTerm == null || searchTerm.isEmpty() || returnToApp != null) {
            logger.info("Empty search term entered for appId: " + appId + " : returning to app view page.");
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
                logger.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            logger.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return new ModelAndView("redirect:/indices/" + appId);
    }

    private String getSearchThreadHtml(List<Thread> threads, String entryBreakString) {
        String currentHtml = "<ul>";

        for (Thread thread : threads) {

            currentHtml += " <li>"
                    + "        <a "
                    + " href=\"/discussion.cgi?disc=" + thread.getApplicationId()
                    + "&article=" + thread.getId() + "\""
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

    private String getAppViewTopThreadHtml(ThreadTreeNode currentNode, String entryBreakString, boolean showPreviewText) {

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
                "&article=" + currentNode.getCurrent().getId() + "\"" +
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
                            "&article=" + currentNode.getCurrent().getId() + "\"" +
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
                                        boolean showPreviewText) {

        if (!skipCurrentNode) {

            currentHtml += "<li class=\"message_entry\">" +
                    " <div class=\"response_headers\">";

            if (isNewMessageHighlighted(currentNode)) {
                currentHtml += "   <span class=\"response_headers new_message\">";
            } else {
                currentHtml += "   <span class=\"response_headers\">";
            }

            //if rendering current thread, subject line should not have an anchor tag.
            if (currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += currentNode.getCurrent().getSubject();
            } else {
                currentHtml += "      <a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                        "&article=" + currentNode.getCurrent().getId() + "\"" +
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
                    "   </span>" +
                    "</div>";

            //only show preview if selected and not currently viewed thread
            if (showPreviewText && !currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                String previewText = currentNode.getCurrent().getBody();
                if (previewText != null && !previewText.isEmpty()) {

                    previewText = inputHelper.sanitizeInput(previewText); //clear HTML from preview thread body

                    if (previewText.length() > 200) {
                        previewText = previewText.substring(0, 200); //todo configurable length
                        previewText +=  "...<a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                                "&article=" + currentNode.getCurrent().getId() + "\"" +
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
            currentHtml = getAppViewThreadHtml(node, currentHtml, entryBreakString, false, currentlyViewedId, showPreviewText);
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
    private String getThreadViewThreadHtml(Thread currentThread) {

        //default reply thread values
        boolean skipCurrent = true;
        boolean isFirstChild = false;
        String currentHtml = "";
        Long grandparentId = 0L;

        //if not top level thread:
        if (currentThread.getParentId() != null && currentThread.getParentId() != 0L) {

            Thread parentThread = threadService.getThread(currentThread.getParentId());
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
            String entryBreakString = configurationService.getStringValue(currentThread.getApplicationId(), ConfigurationProperty.ENTRY_BREAK_TEXT, "-");

            currentHtml += getAppViewThreadHtml(subThreadNode, currentHtml, entryBreakString,
                    skipCurrent, currentThread.getId(), true) ;

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

        logger.warn("Null thread node or create date sent to be checked if highlight functionality should be applied.");
        return false;
    }
}
