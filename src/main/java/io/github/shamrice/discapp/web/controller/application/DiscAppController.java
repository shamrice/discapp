package io.github.shamrice.discapp.web.controller.application;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.account.principal.DiscAppUserPrincipal;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.ApplicationSubscriptionService;
import io.github.shamrice.discapp.service.application.permission.HtmlPermission;
import io.github.shamrice.discapp.service.application.permission.UserPermission;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.UserConfigurationProperty;
import io.github.shamrice.discapp.service.stats.StatisticsService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.service.thread.UserReadThreadService;
import io.github.shamrice.discapp.service.notification.email.type.ReplyNotification;
import io.github.shamrice.discapp.service.notification.email.EmailNotificationQueueService;
import io.github.shamrice.discapp.web.controller.ErrorController;
import io.github.shamrice.discapp.web.define.url.ApplicationSubscriptionUrl;
import io.github.shamrice.discapp.web.model.discapp.NewThreadViewModel;
import io.github.shamrice.discapp.web.model.discapp.ThreadViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.DiscAppHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.url.AppUrl.*;

@Controller
@Slf4j
public class DiscAppController {

    //todo: move to it's own shared properties class. (sort of also used in the thread service class... ish... )
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
    private UserReadThreadService userReadThreadService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private WebHelper webHelper;

    @Autowired
    private DiscAppHelper discAppHelper;

    @Autowired
    private ErrorController errorController;

    @Autowired
    private ApplicationSubscriptionService applicationSubscriptionService;

    @GetMapping(CREATE_THREAD_HOLD)
    public ModelAndView getCreateThreadHoldView(@RequestParam(name="disc") Long appId,
                                                @RequestParam(name="page", required = false) Integer page,
                                                @RequestParam(name="parentId", required = false) Integer parentId,
                                                Model model) {
        if (page == null || page < 0) {
            page = 0;
        }

        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                //if none permissions are set, redirect user to access denied.
                if (accountHelper.checkUserHasPermission(app.getId(), UserPermission.NONE)) {
                    return errorController.getPermissionDeniedView("", model);
                }

                model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));
                model.addAttribute(CURRENT_PAGE, page);
                model.addAttribute(APP_NAME, app.getName());
                model.addAttribute(APP_ID, app.getId());
                model.addAttribute(PARENT_THREAD_ID, parentId);
                model.addAttribute("holdMessageText", configurationService.getStringValue(appId, ConfigurationProperty.HOLD_PERMISSIONS_POST_MESSAGE_TEXT, "Your message will be posted once it has been approved by a moderator."));

                discAppHelper.setButtonModelAttributes(appId, model);

                return new ModelAndView("indices/createThreadHold");
            }
        } catch (Exception ex) {
            model.addAttribute(ERROR, "No disc app with id " + appId + " found. " + ex.getMessage());
            log.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return errorController.getNotFoundView("Disc App with ID of " + appId + " does not exist.", model);

    }

    @GetMapping(ALTERNATE_APPLICATION_VIEW_URL)
    public ModelAndView getAppViewOriginalUrl(@PathVariable(name = "applicationId") Long appId,
                                              @RequestParam(name = "page", required = false) Integer page,
                                              Model model,
                                              HttpServletRequest request) {
        return getAppView(appId, page, model, request);
    }

    @GetMapping(APPLICATION_VIEW_URL)
    public ModelAndView getAppView(@PathVariable(name = "applicationId") Long appId,
                                   @RequestParam(name = "page", required = false) Integer page,
                                   Model model,
                                   HttpServletRequest request) {
        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                //if none permissions are set, redirect user to access denied.
                if (accountHelper.checkUserHasPermission(app.getId(), UserPermission.NONE)) {
                    return errorController.getPermissionDeniedView("", model);
                }

                if (!accountHelper.checkUserHasPermission(app.getId(), UserPermission.POST)) {
                    log.info("User does not have permission to post new messages on appId: "
                            + appId + ". Removing create new message button.");
                    model.addAttribute(NEW_POST_DISABLED, true);
                }

                model.addAttribute(APP_NAME, app.getName());
                model.addAttribute(APP_ID, app.getId());

                model.addAttribute(PROLOGUE_TEXT, applicationService.getPrologueText(app.getId()));
                model.addAttribute(EPILOGUE_TEXT, applicationService.getEpilogueText(app.getId()));

                discAppHelper.setButtonModelAttributes(appId, model);

                if (page == null) {
                    page = 0;
                }
                if (page > 0) {
                    model.addAttribute(HAS_PREVIOUS_PAGE, true);
                    model.addAttribute(PREVIOUS_PAGE, page - 1);
                }

                model.addAttribute(CURRENT_PAGE, page);
                model.addAttribute(NEXT_PAGE, page + 1);
                model.addAttribute(HAS_NEXT_PAGE, true); // default.

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

                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), page, maxThreads, ThreadSortOrder.valueOf(threadSortOrder.toUpperCase()), isExpandOnIndex);

                //if there's no messages... there's no next page. :)
                if (threadTreeNodeList.isEmpty()) {
                    model.addAttribute(HAS_NEXT_PAGE, false);
                }

                //get read threads if user is logged in.
                String[] readThreadsCsv = null;
                Long userId = accountHelper.getLoggedInDiscAppUserId();
                if (userId != null) {
                    readThreadsCsv = userReadThreadService.getReadThreadsCsv(appId, userId);
                }

                if (isExpandOnIndex) {

                    List<String> threadTreeHtml = new ArrayList<>();
                    String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                    int maxPreviewLengthTopLevelThread = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, 320);
                    int maxPreviewLengthReplies = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, 200);
                    int maxThreadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 30);

                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                        String currentHtml = discAppHelper.getAppViewTopThreadHtml(threadTreeNode, entryBreakString, showTopLevelPreview,
                                page, maxPreviewLengthTopLevelThread, readThreadsCsv);

                        //get replies if they exist and add on HTML.
                        if (threadTreeNode.getSubThreads() != null && threadTreeNode.getSubThreads().size() > 0) {
                            currentHtml += "<div class=\"responses\">";
                            currentHtml += discAppHelper.getAppViewThreadHtml(threadTreeNode, "",
                                    entryBreakString, true, -1,
                                    false, page, maxPreviewLengthReplies,
                                    0, maxThreadDepth, readThreadsCsv);
                            currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
                            currentHtml += "</div>";
                        }

                        threadTreeHtml.add(currentHtml);
                    }

                    model.addAttribute(THREAD_NODE_LIST, threadTreeHtml);
                } else {
                    model.addAttribute(DATE_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.DATE_LABEL_TEXT, "Date:"));
                    model.addAttribute(SUBMITTER_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
                    model.addAttribute(SUBJECT_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));

                    List<ThreadViewModel> threads = new ArrayList<>();
                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                        ThreadViewModel threadViewModel = new ThreadViewModel();

                        threadViewModel.setSubmitter(threadTreeNode.getCurrent().getSubmitter());
                        threadViewModel.setSubject(threadTreeNode.getCurrent().getSubject());
                        threadViewModel.setCreateDt(discAppHelper.getAdjustedDateStringForConfiguredTimeZone(appId, threadTreeNode.getCurrent().getCreateDt(), false));
                        threadViewModel.setId(threadTreeNode.getCurrent().getId().toString());
                        threadViewModel.setShowMoreOnPreviewText(false);
                        threadViewModel.setHighlighted(discAppHelper.isNewMessageHighlighted(threadTreeNode));
                        threadViewModel.setAdminPost(threadTreeNode.getCurrent().getIsAdminPost());

                        //mark thread as read or not.
                        threadViewModel.setRead(userReadThreadService.csvContainsThreadId(readThreadsCsv, threadTreeNode.getCurrent().getId()));

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
                    model.addAttribute(THREADS, threads);

                }

                model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute(THREAD_SEPARATOR, configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));
                model.addAttribute(IS_EXPAND_ON_INDEX, isExpandOnIndex);
                model.addAttribute(IS_SHOW_TOP_LEVEL_PREVIEW, showTopLevelPreview);

                return new ModelAndView("indices/appView");

            } else {
                model.addAttribute(ERROR, "Disc app with id " + appId + " returned null.");
                log.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute(ERROR, "No disc app with id " + appId + " found. " + ex.getMessage());
            log.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return errorController.getNotFoundView("Disc App with ID of " + appId + " does not exist.", model);
        //return new ModelAndView("redirect:/error/notfound", "errorText", "Disc App with ID of " + appId + " does not exist.");
    }

    @GetMapping(CREATE_THREAD)
    public ModelAndView getCreateNewThreadRedirect(@RequestParam(name = "disc") Long appId, Model model) {
        log.warn("Attempted GET on create thread page. Redirecting to main view for appId: " + appId);
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @GetMapping(POST_THREAD)
    public ModelAndView getPostThreadRedirect(@RequestParam(name = "disc") Long appId, Model model) {
        log.warn("Attempted GET on post thread page. Redirecting to main view for appId: " + appId);
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @PostMapping(CREATE_THREAD)
    public ModelAndView createNewThread(@RequestParam(name = "disc") Long appId,
                                        @ModelAttribute ThreadViewModel threadViewModel,
                                        @ModelAttribute NewThreadViewModel newThreadViewModel,
                                        HttpServletResponse response,
                                        Model model) {

        //allow caching on create thread POST action to avoid expired web page message.
        response.setHeader("Cache-Control", "max-age=240, private"); // HTTP 1.1

        //check if user has posting permissions when creating a new post.
        if (newThreadViewModel != null) {
            if (newThreadViewModel.getParentId() == null || newThreadViewModel.getParentId().equals("0")) {
                if (!accountHelper.checkUserHasPermission(appId, UserPermission.POST)) {
                    return errorController.getPermissionDeniedView("", model);
                }
            }
        }

        //if user has hold permissions and app is configured to display hold message, show it.
        if (accountHelper.checkUserHasPermission(appId, UserPermission.HOLD) &&
                configurationService.getBooleanValue(appId,
                        ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_MESSAGE, true)) {

            String holdMessage = configurationService.getStringValue(appId,
                    ConfigurationProperty.HOLD_PERMISSIONS_MESSAGE_TEXT,
                    "New messages posted require admin approval. Your message will appear after it has been approved by a moderator.");

            model.addAttribute("holdPermission", holdMessage);
        }

        ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);
        model.addAttribute(ANONYMOUS_POSTING_BLOCKED, applicationPermission.getBlockAnonymousPosting());

        Application app = applicationService.get(appId);

        long parentId = 0L;

        //if coming from thread view page
        if (threadViewModel != null && threadViewModel.getId() != null) {
            try {
                parentId = Long.parseLong(threadViewModel.getId());
                model.addAttribute(PARENT_THREAD_SUBMITTER, threadViewModel.getSubmitter());
                model.addAttribute(PARENT_THREAD_SUBJECT, threadViewModel.getSubject());
                model.addAttribute(PARENT_THREAD_BODY, threadViewModel.getBody());
                model.addAttribute(CURRENT_PAGE, threadViewModel.getCurrentPage());
            } catch (NumberFormatException ex) {
                log.error("Unable to parse parent id from view thread model. appId: " + appId
                        + " : attempted parentId: " + threadViewModel.getId());
            }
        }

        //if coming from preview page
        if (newThreadViewModel != null && newThreadViewModel.getParentId() != null) {
            model.addAttribute(SUBMITTER, newThreadViewModel.getSubmitter());
            model.addAttribute(SUBJECT, newThreadViewModel.getSubject());
            model.addAttribute(EMAIL, newThreadViewModel.getEmail());
            model.addAttribute(BODY, newThreadViewModel.getBody());
            model.addAttribute(SHOW_EMAIL, newThreadViewModel.isShowEmail());
            model.addAttribute(POST_CODE, newThreadViewModel.getPostCode());
            model.addAttribute(PARENT_THREAD_SUBMITTER, newThreadViewModel.getParentThreadSubmitter());
            model.addAttribute(PARENT_THREAD_SUBJECT, newThreadViewModel.getParentThreadSubject());
            model.addAttribute(PARENT_THREAD_BODY, newThreadViewModel.getParentThreadBody());
            model.addAttribute(CURRENT_PAGE, newThreadViewModel.getCurrentPage());
            model.addAttribute(MARK_ADMIN_POST, newThreadViewModel.isMarkAdminPost());

            //preview always returns empty string if checkbox was not previously selected. update accordingly.
            if (newThreadViewModel.getSubscribe() != null && !newThreadViewModel.getSubscribe().trim().isEmpty()) {
                model.addAttribute(SUBSCRIBE, newThreadViewModel.getSubscribe());
            }

            if (newThreadViewModel.getErrorMessage() != null) {
                model.addAttribute(ERROR_MESSAGE, newThreadViewModel.getErrorMessage());
            }

            if (newThreadViewModel.getParentId() != null) {
                try {
                    parentId = Long.parseLong(newThreadViewModel.getParentId());
                } catch (NumberFormatException ex) {
                    parentId = 0L;
                    log.error("Unable to parse parent id from returned new thread model from preview page. appId: " + appId
                            + " : attempted parentId: " + newThreadViewModel.getParentId());
                }
            }
        } else {
            //if coming from app view post new thread or thread view post reply.

            //generate new post code
            String postCode = threadService.generatePostCode(appId);
            model.addAttribute(POST_CODE, postCode);
        }

        //pre-fill form with user info if they are logged in
        if (accountHelper.isLoggedIn()) {
            DiscAppUser user = discAppUserDetailsService.getByEmail(accountHelper.getLoggedInEmail());

            //only set values if not a system account.
            if (user != null && user.getIsUserAccount()) {
                model.addAttribute(IS_LOGGED_IN, true);
                model.addAttribute(SUBMITTER, user.getUsername());
                model.addAttribute(EMAIL, user.getEmail());
                model.addAttribute(SHOW_EMAIL, user.getShowEmail());

                if (applicationService.isUserAccountOwnerOfApp(appId, user.getEmail()) || accountHelper.isRootAdminAccount()) {
                    model.addAttribute("isAdmin", true);
                }
            } else if (user != null && !user.getIsUserAccount()) {
                model.addAttribute(IS_LOGGED_IN_SYSTEM_ACCOUNT, true);
                log.info("User is system admin account. Posting will be treated as not logged in user.");
            }
        }

        model.addAttribute(APP_NAME, app.getName());
        model.addAttribute(APP_ID, appId);
        model.addAttribute(PARENT_THREAD_ID, parentId); //parentThreadId);

        model.addAttribute(SUBMITTER_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
        model.addAttribute(EMAIL_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.EMAIL_LABEL_TEXT, "Email:"));
        model.addAttribute(SUBJECT_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));
        model.addAttribute(BODY_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message Text:"));
        model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

        model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
        model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

        discAppHelper.setButtonModelAttributes(appId, model);

        return new ModelAndView("indices/createThread");
    }

    @PostMapping(PREVIEW_THREAD)
    public ModelAndView postPreviewThread(@RequestParam(name = "disc") Long appId,
                             NewThreadViewModel newThreadViewModel,
                             Model model) {

        Application app = applicationService.get(appId);
        model.addAttribute(APP_NAME, app.getName());
        model.addAttribute(APP_ID, appId);
        model.addAttribute("newThreadViewModel", newThreadViewModel);

        ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);
        model.addAttribute(ANONYMOUS_POSTING_BLOCKED, applicationPermission.getBlockAnonymousPosting());

        String htmlBody = newThreadViewModel.getBody();
        if (htmlBody != null && !htmlBody.isEmpty()) {
            htmlBody = htmlBody.replaceAll("\r", "<br />");
        }
        newThreadViewModel.setHtmlBody(htmlBody);

        discAppHelper.setButtonModelAttributes(appId, model);

        model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
        model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

        model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
        model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

        return new ModelAndView("indices/previewThread", "model", model);
    }

    @PostMapping(POST_THREAD)
    public ModelAndView postNewThread(@RequestParam(name = "disc") Long appId,
                                      @ModelAttribute NewThreadViewModel newThreadViewModel,
                                      ThreadViewModel threadViewModel,
                                      Model model,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (newThreadViewModel != null) {

            if (newThreadViewModel.getReturnToApp() != null) {
                log.info("Return to app button clicked for app id " + appId + ". Value=" + newThreadViewModel.getReturnToApp());

                if (newThreadViewModel.getCurrentPage() != null && newThreadViewModel.getCurrentPage() > 0) {
                    int page = newThreadViewModel.getCurrentPage();
                    return new ModelAndView("redirect:/indices/" + appId + "?page=" + page);
                }
                return new ModelAndView("redirect:/indices/" + appId);

            } else if (newThreadViewModel.getPreviewArticle() != null) {
                return postPreviewThread(appId, newThreadViewModel, model);

            } else if (newThreadViewModel.getSubmitNewThread() != null) {

                int page = 0;
                if (newThreadViewModel.getCurrentPage() != null && newThreadViewModel.getCurrentPage() > 0) {
                    page = newThreadViewModel.getCurrentPage();
                }

                if ((newThreadViewModel.getSubject() == null || newThreadViewModel.getSubject().trim().isEmpty())
                        && (newThreadViewModel.getBody() == null || newThreadViewModel.getBody().trim().isEmpty())) {
                    log.warn("AppId: " + appId + " cannot create new thread with no subject and no body.");
                    return new ModelAndView("redirect:/indices/" + appId + "?page=" + page);
                }

                //set ip address and user agent
                String ipAddress = null;
                String userAgent = null;
                if (request != null) {
                    //check forwarded header for proxy users, if not found, use ip provided.
                    ipAddress = request.getHeader("X-FORWARDED-FOR");
                    if (ipAddress == null || ipAddress.isEmpty()) {
                        ipAddress = request.getRemoteAddr();
                    }
                    userAgent = request.getHeader(HttpHeaders.USER_AGENT);
                }

                if (threadService.isNewThreadPostTooSoon(appId, ipAddress)) {
                    log.error("Cannot create thread so soon after creating previous thread. Returning user to page with error.");
                    //TODO : centralize error messages.
                    newThreadViewModel.setErrorMessage("New message too soon after previous post. Please try again.");
                    return createNewThread(appId, null, newThreadViewModel, response, model);
                }


                ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);

                //set submitter to anon if not filled out
                String submitter = "";
                if (newThreadViewModel.getSubmitter() == null || newThreadViewModel.getSubmitter().trim().isEmpty()) {
                    if (applicationPermission.getBlockAnonymousPosting()) {
                        log.error("New message was attempted with no submitter but anonymous posting is disabled for appId: " + appId + " :: postcode: " + newThreadViewModel.getPostCode());
                        newThreadViewModel.setErrorMessage("Author field is required to post a message.");
                        return createNewThread(appId, null, newThreadViewModel, response, model);
                    } else {
                        submitter = BLANK_SUBMITTER;
                    }
                } else {
                    submitter = newThreadViewModel.getSubmitter();
                }

                //redeem post code.
                if (!threadService.redeemPostCode(appId, newThreadViewModel.getPostCode())) {
                    log.error("Invalid post code attempted to be redeemed for appId: " + appId + " :: postcode: " + newThreadViewModel.getPostCode());
                    newThreadViewModel.setErrorMessage("You have already posted this message.");
                    return createNewThread(appId, null, newThreadViewModel, response, model);
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

                String email = newThreadViewModel.getEmail();
                String body = newThreadViewModel.getBody();



                //sanitize inputs based on settings.
                if (applicationPermission != null) {
                    String htmlPermissions = applicationPermission.getAllowHtmlPermissions();

                    if (HtmlPermission.BLOCK_SUBJECT_SUBMITTER_FIELDS.equalsIgnoreCase(htmlPermissions) || HtmlPermission.FORBID.equalsIgnoreCase(htmlPermissions)) {
                        submitter = inputHelper.convertHtmlToPlainText(submitter);
                        email = inputHelper.convertHtmlToPlainText(email);
                        subject = inputHelper.convertHtmlToPlainText(subject);
                    } else {
                        //remove css and script tags regardless of permissions.
                        submitter = inputHelper.convertScriptAndStyleTags(submitter);
                        email = inputHelper.convertScriptAndStyleTags(email);
                        subject = inputHelper.convertScriptAndStyleTags(subject);
                    }
                    if (HtmlPermission.FORBID.equalsIgnoreCase(htmlPermissions)) {
                        body = inputHelper.convertHtmlToPlainText(body);
                    } else {
                        //remove css and script tags regardless of permissions.
                        body = inputHelper.convertScriptAndStyleTags(body);
                    }
                } else {
                    //if no permissions exist (for some reason...) default to blocking all HTML.
                    log.warn("No application permissions exist for appId: " + appId
                            + " defaulting to HTML permission: " + HtmlPermission.FORBID);
                    submitter = inputHelper.convertHtmlToPlainText(submitter);
                    email = inputHelper.convertHtmlToPlainText(email);
                    subject = inputHelper.convertHtmlToPlainText(subject);
                    body = inputHelper.convertHtmlToPlainText(body);
                }

                log.info("new thread: " + newThreadViewModel.getAppId() + " : " + submitter + " : "
                        + subject + " : " + body);

                boolean isApproved = true;
                if (accountHelper.checkUserHasPermission(appId, UserPermission.HOLD)) {
                    log.info("New thread must be approved before being visible. Setting isApproved to false for subject: "
                            + subject + " on appId; " + appId);
                    isApproved = false;
                }

                Thread newThread = new Thread();
                newThread.setApplicationId(appId);
                newThread.setParentId(parentId);
                newThread.setDeleted(false);
                newThread.setCreateDt(new Date());
                newThread.setModDt(new Date());
                newThread.setSubject(subject);
                newThread.setIpAddress(ipAddress);
                newThread.setUserAgent(userAgent);
                newThread.setApproved(isApproved);

                //set values for logged in user, if not logged in... use form data.
                String userEmail = accountHelper.getLoggedInEmail();

                DiscAppUser discAppUser = discAppUserDetailsService.getByEmail(userEmail);
                //only set values for logged in users who are not system accounts.
                if (discAppUser != null && discAppUser.getIsUserAccount()) {
                    newThread.setDiscAppUser(discAppUser);
                    newThread.setSubmitter(discAppUser.getUsername());
                    newThread.setEmail(discAppUser.getEmail());
                    newThread.setShowEmail(discAppUser.getShowEmail());
                    newThread.setIsAdminPost(newThreadViewModel.isMarkAdminPost());

                } else {
                    newThread.setSubmitter(submitter);
                    newThread.setIsAdminPost(false);

                    //only set email if it's valid.
                    if (!email.isEmpty() && EmailValidator.getInstance().isValid(email)) {
                        newThread.setEmail(email);

                        //only use input from checkbox if there's an email address entered
                        newThread.setShowEmail(newThreadViewModel.isShowEmail());
                    } else {
                        newThread.setEmail("");
                        newThread.setShowEmail(false);
                    }
                }

                //add links to urls and add new lines.
                if (body != null && !body.trim().isEmpty()) {
                    body = body.replaceAll("\r", "<br />");
                    body = inputHelper.addUrlHtmlLinksToString(body);
                }

                Long newThreadId = threadService.saveThread(newThread, body, newThread.isApproved());
                if (newThreadId != null) {

                    //send reply notification email if enabled to parent thread being replied to.
                    try {
                        if (parentId != 0L) {
                            boolean isReplyNotificationsEnabled = configurationService.getBooleanValue(appId, ConfigurationProperty.EMAIL_REPLY_NOTIFICATION_ENABLED, false);

                            if (isReplyNotificationsEnabled) {

                                Thread parentThread = threadService.getThread(appId, parentId);

                                if (parentThread != null && parentThread.getEmail() != null && !parentThread.getEmail().trim().isEmpty()) {

                                    if (EmailValidator.getInstance().isValid(parentThread.getEmail().trim())) {

                                        //email to non-disc app user replies or only enabled disc app users.
                                        DiscAppUser user = parentThread.getDiscAppUser();
                                        boolean userRepliesEnabled = true;
                                        if (user != null) {
                                            userRepliesEnabled = configurationService.getUserConfigBooleanValue(user.getId(), UserConfigurationProperty.USER_REPLY_NOTIFICATION_ENABLED, true);
                                        }

                                        if (user == null || (user.getEnabled() && userRepliesEnabled)) {

                                            Application app = applicationService.get(appId);
                                            String discussionFullUrl = webHelper.getBaseUrl(request) + "/" + DISCUSSION_URL;

                                            ReplyNotification replyNotification = new ReplyNotification(appId, app.getName(), discussionFullUrl, parentThread.getEmail(), newThreadId);
                                            EmailNotificationQueueService.addReplyToSend(replyNotification);
                                        } else {
                                            log.info("Reply notification not sent to: " + parentThread.getEmail() + " : Reply notifications are disabled for that user.");
                                        }

                                    } else {
                                        log.warn("Reply notification failed for email: " + parentThread.getEmail() + " : email address is not valid.");
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        log.error("Failed to send reply notification email: " + t.getMessage(), t);
                    }

                    //redirect them to subscribe url if they clicked subscribe.
                    if (email != null && !email.isEmpty() && newThreadViewModel.getSubscribe() != null && !newThreadViewModel.getSubscribe().isEmpty()) {

                        //url encode email address
                        String urlEmail = UriUtils.encode(email, StandardCharsets.UTF_8);

                        return new ModelAndView("redirect:" + ApplicationSubscriptionUrl.SUBSCRIBE_URL
                                + "?id=" + appId + "&email=" + urlEmail + "&encoded=true");
                    }

                    //if app is being held until approval, redirect to message letting the user know if configured to do so.
                    if (!isApproved
                            && configurationService.getBooleanValue(appId,
                            ConfigurationProperty.HOLD_PERMISSIONS_DISPLAY_POST_MESSAGE, true)) {
                        return new ModelAndView("redirect:/createThreadHold?disc=" + appId + "&page=" + page + "&parentId=" + parentId);
                    }

                    //otherwise, give view thread posted page.
                    String threadSortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.CREATION.name());
                    if (ThreadSortOrder.CREATION.name().equalsIgnoreCase(threadSortOrder)) {
                        //if disc app sorted by creation, include page num
                        return new ModelAndView("redirect:/discussion.cgi?disc=" + appId + "&article=" + newThreadId + "&page=" + page);
                    } else {
                        //if sorted by activity, don't include page num as thread will be on first page now.
                        return new ModelAndView("redirect:/discussion.cgi?disc=" + appId + "&article=" + newThreadId);
                    }
                }
            }
        }
        log.info("Error posting thread or couldn't find redirect action for POST. Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @GetMapping(DISCUSSION_URL)
    public String getViewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "article", required = false) Long threadId,
                                @RequestParam(name = "page", required = false) Integer currentPage,
                                Model model) {

        if (!accountHelper.checkUserHasPermission(appId, UserPermission.READ)) {
            return "/error/permissionDenied";
        }

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
        if (currentThread != null && currentThread.isApproved()) {

            String[] readThreadsCsv = null;

            //mark thread as read if user is logged in.
            DiscAppUserPrincipal userPrincipal = accountHelper.getLoggedInUser();
            if (userPrincipal != null && userPrincipal.isUserAccount()) {
                log.info("User logged in. Marking thread: " + threadId + " as read for userId: " + userPrincipal.getId()
                        + " on appId: " + appId + " :: and getting list of read threads");
                userReadThreadService.markThreadAsRead(appId, userPrincipal.getId(), threadId);

                //get list of read threads.
                readThreadsCsv = userReadThreadService.getReadThreadsCsv(appId, userPrincipal.getId());
            }

            int maxPreviewLength = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, 200);
            int maxThreadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 30);

            String threadBody = threadService.getThreadBodyText(threadId);
            String subThreadsHtml = getThreadViewThreadHtml(currentThread, currentPage, maxPreviewLength, maxThreadDepth, readThreadsCsv);

            DiscAppUser sourceUser = currentThread.getDiscAppUser();

            try {
                //set current username if exists and different than in thread table.
                if (sourceUser != null && !currentThread.getSubmitter().equals(sourceUser.getUsername())) {
                    threadViewModel.setCurrentUsername(sourceUser.getUsername());
                }
            } catch (EntityNotFoundException notFound) {
                log.warn("DiscApp User Id not found for threadId " + currentThread.getId() + " :: " + notFound.getMessage(), notFound);
                sourceUser = null;
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
            threadViewModel.setAdminPost(currentThread.getIsAdminPost());

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
            threadViewModel.setCreateDt(discAppHelper.getAdjustedDateStringForConfiguredTimeZone(appId, currentThread.getCreateDt(), true));

            //check app permissions if ip address should be shown:
            ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(appId);
            if (applicationPermission != null) {
                threadViewModel.setShowIpAddress(applicationPermission.getDisplayIpAddress());
            } else {
                //default to true
                threadViewModel.setShowIpAddress(true);
            }

            Application app = applicationService.get(appId);
            model.addAttribute(APP_NAME, app.getName());

            model.addAttribute("threadViewModel", threadViewModel);
            model.addAttribute(SUB_THREADS_HTML, subThreadsHtml);

            discAppHelper.setButtonModelAttributes(appId, model);

            model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
            model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

            model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
            model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

            model.addAttribute(SUBSCRIBE_URL, ApplicationSubscriptionUrl.SUBSCRIBE_URL + "?id=" + appId);

            //remove reply button if user doesn't have reply permissions.
            if (!accountHelper.checkUserHasPermission(app.getId(), UserPermission.REPLY)) {
                log.info("User does not have permission to reply on appId: " + appId + ". Disabling reply button.");
                model.addAttribute(REPLY_DISABLED, true);
            }

        } else {
            log.warn("Attempted to view thread: " + threadId + " on appId: " + appId + " but does not belong to app or is not approved. Redirecting to appView.");
            return "redirect:/indices/" + appId;
        }

        return "indices/viewThread";
    }

    @PostMapping(DISCUSSION_URL)
    public ModelAndView postDiscussionForm(@RequestParam(name = "disc") Long appId,
                                           ThreadViewModel threadViewModel,
                                           HttpServletResponse response,
                                           Model model) {
        if (threadViewModel != null) {
            if (threadViewModel.getReturnToApp() != null) {
                int currentPage = 0;
                if (threadViewModel.getCurrentPage() != null && threadViewModel.getCurrentPage() > 0) {
                    currentPage = threadViewModel.getCurrentPage();
                }

                log.info("Return to app button clicked for app id " + appId + ". Value=" + threadViewModel.getReturnToApp());
                return new ModelAndView("redirect:/indices/" + appId + "?page=" + currentPage + "#" + threadViewModel.getId());

            } else if (threadViewModel.getPostResponse() != null) {

                //make sure user has reply permissions before allowing reply to be created.
                if (!accountHelper.checkUserHasPermission(appId, UserPermission.REPLY)) {
                    return errorController.getPermissionDeniedView("", model);
                }

                log.info("new reply started to appId: " + appId + " thread id : " + threadViewModel.getId()
                        + " submitter: " + threadViewModel.getSubmitter() + " : subject: "
                        + threadViewModel.getSubject() + " : email: " + threadViewModel.getEmail()
                        + " : body: " + threadViewModel.getBody());

                return createNewThread(appId, threadViewModel, null, response, model);
            }
        }

        log.info("Fallback return to thread view.");
        return new ModelAndView("redirect:/indices/" + appId);
    }

    @GetMapping(APP_SEARCH_URL)
    public ModelAndView getSearchDiscApp(@RequestParam(name = "disc") Long appId,
                                         Model model) {
        log.debug("Attempted GET on search. Only POST is allowed. AppId: " + appId);
        return new ModelAndView("redirect:/indices/" + appId);
    }


    @PostMapping(APP_SEARCH_URL)
    public ModelAndView searchDiscApp(@RequestParam(name = "disc") Long appId,
                                      @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                      @RequestParam(name = "searchAgain", required = false) String searchAgain,
                                      @RequestParam(name = "returnToApp", required = false) String returnToApp,
                                      @RequestParam(name = "page", required = false) Integer page,
                                      @RequestParam(name = "nextPage", required = false) String nextPage,
                                      @RequestParam(name = "previousPage", required = false) String previousPage,
                                      HttpServletResponse response,
                                      Model model) {

        if (searchTerm == null || searchTerm.isEmpty() || returnToApp != null) {
            log.info("Empty search term entered for appId: " + appId + " : returning to app view page.");
            return new ModelAndView("redirect:/indices/" + appId);
        }

        //allow caching on search POST action to avoid expired web page message if back button is pressed from linked thread.
        response.setHeader("Cache-Control", "max-age=240, private"); // HTTP 1.1

        //if page is not set or they clicked the search again button, reset page to zero
        if ((page == null || page < 0) || (searchAgain != null && !searchAgain.isEmpty())) {
            page = 0;
        }

        if (nextPage != null && !nextPage.isEmpty()) {
            page++;
        } else if (previousPage != null && !previousPage.isEmpty() && page > 0) {
            page--;
        }

        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute(APP_NAME, app.getName());
                model.addAttribute(APP_ID, app.getId());
                model.addAttribute("page", page);

                //get search results
                List<Thread> foundThreads = threadService.searchThreads(appId, searchTerm, page, 20);
                String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                model.addAttribute(SEARCH_RESULTS, getSearchThreadHtml(foundThreads, entryBreakString));
                model.addAttribute(SEARCH_TERM, searchTerm);

                model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute(THREAD_SEPARATOR, configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/disc_" + appId + ".css"));

                model.addAttribute(HAS_NEXT_PAGE, foundThreads.size() == 20);
                model.addAttribute(HAS_PREVIOUS_PAGE, page > 0);

                return new ModelAndView("indices/search", "model", model);

            } else {
                model.addAttribute(ERROR, "Disc app with id " + appId + " returned null.");
                log.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute(ERROR, "No disc app with id " + appId + " found. " + ex.getMessage());
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
                            discAppHelper.getAdjustedDateStringForConfiguredTimeZone(
                                thread.getApplicationId(),
                                thread.getCreateDt(), true) +
                    "</span> "
                    + "</li>";
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
    private String getThreadViewThreadHtml(Thread currentThread, int currentPage, int maxPreviewLength,
                                           int maxThreadDepth, String[] readThreads) {

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
                    maxThreadDepth += 2; //starting back two levels, so add to depth allowance.
                } else {
                    //if grandparent is top level thread... set to parent
                    grandparentId = parentThread.getId();
                    skipCurrent = false;
                    isFirstChild = true;
                    maxThreadDepth += 1; //starting back one level so add to depth allowance.

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

            currentHtml += discAppHelper.getAppViewThreadHtml(subThreadNode, currentHtml, entryBreakString,
                    skipCurrent, currentThread.getId(), true, currentPage, maxPreviewLength,
                    0, maxThreadDepth, readThreads);

            //first child thread needs additional html tags added
            if (isFirstChild) {
                currentHtml = "<div class=\"nested_list\"><ul>" + currentHtml + "</div>";
            } else {
                currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
            }
        }

        return currentHtml;
    }

}
