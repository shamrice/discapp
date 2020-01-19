package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.maintenance.MaintenanceThreadViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.POSTING_USERNAME;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.THREADS_EDIT_PAGE;
import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.THREAD_EDIT_PAGE;

@Controller
@Slf4j
public class ThreadsMaintenanceController extends MaintenanceController {

    @PostMapping(THREADS_EDIT_PAGE)
    public ModelAndView postDiscEditView(HttpServletRequest request,
                                         @RequestParam(name = "id") long appId,
                                         @RequestParam(name = "tab", required = false) String currentTab,
                                         @RequestParam(name = "pagemark", required = false) Long pageMark,
                                         @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                         Model model,
                                         HttpServletResponse response) {

        //allow caching on create thread POST action to avoid expired web page message.
        response.setHeader("Cache-Control", "max-age=240, private"); // HTTP 1.1

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);

            //delete threads
            if (maintenanceThreadViewModel.getDeleteArticles() != null && !maintenanceThreadViewModel.getDeleteArticles().isEmpty()) {

                boolean deleteThreadsSuccess = true;

                //delete from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), false)) {
                        log.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        deleteThreadsSuccess = false;
                    }
                } else {
                    //delete from thread view or search view with checkboxes.
                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.deleteThread(app.getId(), threadId, false)) {
                                log.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId());
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
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //delete threads and replies
            if (maintenanceThreadViewModel.getDeleteArticlesAndReplies() != null && !maintenanceThreadViewModel.getDeleteArticlesAndReplies().isEmpty()) {

                boolean deleteThreadsAndRepliesSuccess = true;

                //delete from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.deleteThread(app.getId(), maintenanceThreadViewModel.getEditArticleId(), true)) {
                        log.error("Failed to delete thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId() + " and replies.");
                        deleteThreadsAndRepliesSuccess = false;
                    }
                } else {
                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.deleteThread(app.getId(), threadId, true)) {
                                log.error("Failed to delete thread id: " + threadId + " for appId: " + app.getId() + " and replies.");
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
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //make this a top level article
            if (maintenanceThreadViewModel.getMakeThisTopLevelArticle() != null && !maintenanceThreadViewModel.getMakeThisTopLevelArticle().isEmpty()
                    && maintenanceThreadViewModel.getEditArticleParentId() > 0) {


                boolean makeThreadTopLevelSuccess = true;

                //set top level from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.setThreadAsTopLevelArticle(app.getId(), maintenanceThreadViewModel.getEditArticleId())) {
                        log.error("Failed to mark thread as top level thread: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        makeThreadTopLevelSuccess = false;
                    }
                }

                //set message for user
                if (makeThreadTopLevelSuccess) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully made message top level article.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to make message top level article.");
                }
                //return to thread list
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //report abuse
            if (maintenanceThreadViewModel.getReportAbuse() != null && !maintenanceThreadViewModel.getReportAbuse().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                boolean threadsReportedSuccess = true;

                //report abuse from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    if (!threadService.reportThreadForAbuse(app.getId(), maintenanceThreadViewModel.getEditArticleId(), user.getId())) {
                        log.error("Failed to report thread id: " + maintenanceThreadViewModel.getEditArticleId() + " for appId: " + app.getId());
                        threadsReportedSuccess = false;
                    }
                } else {

                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            if (!threadService.reportThreadForAbuse(app.getId(), threadId, user.getId())) {
                                log.error("Failed to report thread id: " + threadId + " for appId: " + app.getId());
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
                //set view back to thread list view
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //approve message
            if (maintenanceThreadViewModel.getApprove() != null && !maintenanceThreadViewModel.getApprove().isEmpty()) {

                boolean threadApproved = true;

                //report abuse from edit message screen
                if (maintenanceThreadViewModel.isOnEditMessage() && maintenanceThreadViewModel.getEditArticleId() != null) {
                    Thread thread = threadService.getThreadById(maintenanceThreadViewModel.getEditArticleId());
                    if (thread != null && thread.getApplicationId().equals(app.getId())) {
                        thread.setApproved(true);
                        thread.setModDt(new Date());
                        if (threadService.saveThread(thread, thread.getBody()) == null) {
                            log.error("Failed to approve thread: " + thread.toString());
                            threadApproved = false;
                        } else {
                            log.info("Thread marked as approved by user: " + username + " :: " + thread.toString());
                        }
                    }
                } else {

                    if (maintenanceThreadViewModel.getSelectThreadCheckbox() != null) {
                        for (String threadIdStr : maintenanceThreadViewModel.getSelectThreadCheckbox()) {
                            long threadId = Long.parseLong(threadIdStr);
                            Thread thread = threadService.getThreadById(threadId);
                            if (thread != null && thread.getApplicationId().equals(app.getId())) {
                                thread.setApproved(true);
                                thread.setModDt(new Date());
                                if (threadService.saveThread(thread, thread.getBody()) == null) {
                                    log.error("Failed to approve thread: " + thread.toString());
                                    threadApproved = false;
                                } else {
                                    log.info("Thread marked as approved by user: " + username + " :: " + thread.toString());
                                }
                            } else {
                                log.warn("Thread id: " + threadIdStr + " for appId: " + app.getId()
                                        + " Either does not exist or does not belong to thread. Cannot be approved.");
                            }
                        }
                    }
                }

                if (threadApproved) {
                    maintenanceThreadViewModel.setInfoMessage("Successfully approved messages.");
                } else {
                    maintenanceThreadViewModel.setInfoMessage("Failed to approve messages.");
                }
                //set view back to thread list view
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

            //search messages
            if ((maintenanceThreadViewModel.getFindMessages() != null
                    && !maintenanceThreadViewModel.getFindMessages().isEmpty())
                    || (maintenanceThreadViewModel.isSearchSubmitted())) {
/*                    && (maintenanceThreadViewModel.getNextPageSubmit() != null || maintenanceThreadViewModel.getPreviousPageSubmit() != null))
                    || (maintenanceThreadViewModel.isSearchSubmitted()
                    && maintenanceThreadViewModel.getEditArticleCancelEdit() != null)
            ) {
*/
                //set off of unapproved so default is approved if user does not have permission to search unapproved threads
                boolean isApproved = !"unapproved".equalsIgnoreCase(maintenanceThreadViewModel.getApprovedSearch());

                //weird paging for search because it goes post code first, then get. backwards from other tabs for
                //next and previous pages.
                int pageToSearch = 0;

                if (maintenanceThreadViewModel.getNextPageSubmit() != null) {
                    pageToSearch = maintenanceThreadViewModel.getCurrentPage() + 1;
                } else if (maintenanceThreadViewModel.getPreviousPageSubmit() != null) {
                    pageToSearch = maintenanceThreadViewModel.getCurrentPage() - 1;
                }

                //new search. reset current page number.
                if (maintenanceThreadViewModel.getFindMessages() != null || pageToSearch < 0) {
                    pageToSearch = 0;
                }

                Page<Thread> pagedSearchResults = threadService.searchThreadsByFields(
                        app.getId(),
                        maintenanceThreadViewModel.getAuthorSearch(),
                        maintenanceThreadViewModel.getEmailSearch(),
                        maintenanceThreadViewModel.getSubjectSearch(),
                        maintenanceThreadViewModel.getIpSearch(),
                        maintenanceThreadViewModel.getMessageSearch(),
                        isApproved,
                        pageToSearch,
                        20
                );

                List<Thread> threadList = pagedSearchResults.get().collect(Collectors.toList());

                String searchResultsHtml = getListThreadHtml(
                        threadList,
                        SEARCH_TAB,
                        true,
                        maintenanceThreadViewModel.getAuthorSearch(),
                        maintenanceThreadViewModel.getEmailSearch(),
                        maintenanceThreadViewModel.getSubjectSearch(),
                        maintenanceThreadViewModel.getIpSearch(),
                        maintenanceThreadViewModel.getMessageSearch(),
                        maintenanceThreadViewModel.getApprovedSearch()
                );
                List<String> threadHtml = new ArrayList<>();
                threadHtml.add(searchResultsHtml);
                maintenanceThreadViewModel.setEditThreadTreeHtml(threadHtml);
                maintenanceThreadViewModel.setNumberOfMessages(pagedSearchResults.getTotalElements());
                maintenanceThreadViewModel.setSearchSubmitted(true);

                if (pagedSearchResults.getTotalElements() > 20) {
                    maintenanceThreadViewModel.setHasNextPage(true);
                }
            }

            //search again
            if (maintenanceThreadViewModel.getSearchAgain() != null && !maintenanceThreadViewModel.getSearchAgain().isEmpty()) {
                maintenanceThreadViewModel.setSearchSubmitted(false);
                maintenanceThreadViewModel.setTab(SEARCH_TAB);
            }

            //post new message
            if (maintenanceThreadViewModel.getPostArticle() != null && !maintenanceThreadViewModel.getPostArticle().isEmpty()) {

                DiscAppUser user = discAppUserDetailsService.getByEmail(username);

                if (user != null && user.getIsUserAccount()) {

                    if (maintenanceThreadViewModel.getNewThreadSubject() != null
                            && !maintenanceThreadViewModel.getNewThreadSubject().trim().isEmpty()) {

                        String subject = inputHelper.sanitizeInput(maintenanceThreadViewModel.getNewThreadSubject());


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

                        Thread newThread = new Thread();
                        newThread.setSubmitter(user.getUsername());
                        newThread.setDiscAppUser(user);
                        newThread.setParentId(0L);
                        newThread.setShowEmail(user.getShowEmail());
                        newThread.setEmail(user.getEmail());
                        newThread.setDeleted(false);
                        newThread.setApplicationId(app.getId());
                        newThread.setSubject(subject);
                        newThread.setModDt(new Date());
                        newThread.setCreateDt(new Date());
                        //newThread.setApproved(isApproved);
                        newThread.setApproved(true);
                        newThread.setIpAddress(ipAddress);
                        newThread.setUserAgent(userAgent);

                        String body = maintenanceThreadViewModel.getNewThreadMessage();
                        if (body != null && !body.isEmpty()) {
                            body = body.replaceAll("\r", "<br />");

                            body = inputHelper.addUrlHtmlLinksToString(body);
                        }

                        threadService.saveThread(newThread, body);
                    } else {
                        maintenanceThreadViewModel.setInfoMessage("A subject is required to post a new message.");
                    }
                } else {
                    //TODO : maintenance accounts should post as logged out users. Page should ask for name/email
                    //TODO : etc just like regular disc app does.
                    maintenanceThreadViewModel.setInfoMessage("Maintenance system accounts cannot post new messages here. Please log in with a registered user account.");
                }
                //return to thread tab.
                maintenanceThreadViewModel.setTab(THREAD_TAB);
                currentTab = THREAD_TAB;
            }

            //selected to modify a single message
            if (maintenanceThreadViewModel.getEditArticle() != null && !maintenanceThreadViewModel.getEditArticle().isEmpty() && maintenanceThreadViewModel.isOnEditMessage()) {

                Thread threadToEdit = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());

                if (threadToEdit != null && threadToEdit.getApplicationId().equals(app.getId())) {
                    maintenanceThreadViewModel.setEditArticleSubmitter(threadToEdit.getSubmitter());
                    maintenanceThreadViewModel.setEditArticleEmail(threadToEdit.getEmail());
                    maintenanceThreadViewModel.setEditArticleSubject(threadToEdit.getSubject());
                    maintenanceThreadViewModel.setEditArticleMessage(threadToEdit.getBody());
                    maintenanceThreadViewModel.setApplicationId(app.getId());

                    String threadBody = threadService.getThreadBodyText(threadToEdit.getId());
                    maintenanceThreadViewModel.setEditArticleMessage(threadBody);

                    ThreadTreeNode subThreads = threadService.getFullThreadTree(threadToEdit.getId());
                    if (subThreads != null) {
                        String subThreadHtml = getEditThreadHtml(subThreads, "", true, false,
                                maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab(),
                                maintenanceThreadViewModel.isSearchSubmitted());
                        maintenanceThreadViewModel.setEditArticleReplyThreadsHtml(subThreadHtml);
                    }

                    maintenanceThreadViewModel.setOnEditModifyMessage(true);

                } else {
                    log.warn("Failed to find thread to edit: " + maintenanceThreadViewModel.getEditArticleId() + " : appId: " + app.getId());
                    maintenanceThreadViewModel.setOnEditModifyMessage(false);
                    maintenanceThreadViewModel.setOnEditMessage(false);
                    maintenanceThreadViewModel.setInfoMessage("An error occurred loading message to edit. Please try again.");
                }
            }

            //submitted modified message
            if (maintenanceThreadViewModel.getEditArticleChangeMessage() != null && !maintenanceThreadViewModel.getEditArticleChangeMessage().isEmpty()
                    && maintenanceThreadViewModel.isOnEditModifyMessage()) {

                Thread editThread = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());
                if (editThread != null && editThread.getApplicationId().equals(app.getId())) {
                    String submitter = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleSubmitter());
                    String email = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleEmail());
                    String subject = inputHelper.sanitizeInput(maintenanceThreadViewModel.getEditArticleSubject());

                    editThread.setSubmitter(submitter);
                    editThread.setEmail(email);
                    editThread.setSubject(subject);
                    editThread.setModDt(new Date());

                    String body = maintenanceThreadViewModel.getEditArticleMessage();

                    if (threadService.saveThread(editThread, body) != null) {
                        maintenanceThreadViewModel.setInfoMessage("Successfully edited thread.");
                    } else {
                        log.error("Failed to edit thread: " + maintenanceThreadViewModel.getEditArticleId() + " : appId: " + app.getId());
                        maintenanceThreadViewModel.setInfoMessage("Failed to update thread.");
                    }

                    maintenanceThreadViewModel.setOnEditModifyMessage(false);
                    maintenanceThreadViewModel.setOnEditMessage(false);
                }
            }

            //cancel button clicked on edit modify message screen
            if (maintenanceThreadViewModel.getEditArticleCancelEdit() != null
                    && !maintenanceThreadViewModel.getEditArticleCancelEdit().isEmpty()) {
                maintenanceThreadViewModel.setOnEditModifyMessage(false);
                maintenanceThreadViewModel.setOnEditMessage(false);

            }

        } catch (Exception ex) {
            log.error("Thread administration action failed: " + ex.getMessage(), ex);
            maintenanceThreadViewModel.setInfoMessage("Error has occurred. Please try again.");
        }

        if (!maintenanceThreadViewModel.isOnEditMessage()) {
            maintenanceThreadViewModel.setOnEditMessage(false);
        }

        return getDiscEditView(appId, currentTab, maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel, model, response);
    }

    @GetMapping(THREADS_EDIT_PAGE)
    public ModelAndView getDiscEditView(@RequestParam(name = "id") long appId,
                                        @RequestParam(name = "tab", required = false) String currentTab,
                                        @RequestParam(name = "page", required = false) Integer page,
                                        @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                        Model model,
                                        HttpServletResponse response) {

        if (currentTab == null || currentTab.isEmpty()) {
            currentTab = THREAD_TAB;
        }

        maintenanceThreadViewModel.setTab(currentTab);

        if (page != null && page >= 0) {
            maintenanceThreadViewModel.setCurrentPage(page);
        }

        //set page to 0 if it's not already set
        if (maintenanceThreadViewModel.getCurrentPage() == null || maintenanceThreadViewModel.getCurrentPage() < 0) {
            maintenanceThreadViewModel.setCurrentPage(0);
        }

        //update page to next or previous if those buttons were selected.
        if (maintenanceThreadViewModel.getNextPageSubmit() != null) {
            maintenanceThreadViewModel.setCurrentPage(maintenanceThreadViewModel.getNextPage());
        }
        if (maintenanceThreadViewModel.getPreviousPageSubmit() != null) {
            maintenanceThreadViewModel.setCurrentPage(maintenanceThreadViewModel.getPreviousPage());
        }

        //if current page is greater than 0, there is a previous page..
        if (maintenanceThreadViewModel.getCurrentPage() > 0) {
            maintenanceThreadViewModel.setHasPreviousPage(true);
        }

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceThreadViewModel.setApplicationId(app.getId());

            DiscAppUser user = discAppUserDetailsService.getByEmail(username);
            model.addAttribute(POSTING_USERNAME, user.getUsername());

            //enable the unapproved messages tab if they exist.
            List<Thread> unapprovedThreads = threadService.getUnapprovedThreads(app.getId());
            if (unapprovedThreads != null && unapprovedThreads.size() > 0) {
                maintenanceThreadViewModel.setHasUnapprovedMessages(true);
            }

            if (!maintenanceThreadViewModel.getTab().equals(SEARCH_TAB) && !maintenanceThreadViewModel.getTab().equals(UNAPPROVED_TAB)) {
                //get edit threads html
                List<String> threadTreeHtml = new ArrayList<>();

                List<ThreadTreeNode> threadTreeNodeList = null;

                if (maintenanceThreadViewModel.getTab().equals(THREAD_TAB)) {

                    threadTreeNodeList = threadService.getLatestThreads(app.getId(), maintenanceThreadViewModel.getCurrentPage(), 20, ThreadSortOrder.CREATION, true);

                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                        String currentHtml = getEditThreadHtml(threadTreeNode, "<ul>", false, true,
                                maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab(), maintenanceThreadViewModel.isSearchSubmitted());
                        currentHtml += "</ul>";
                        threadTreeHtml.add(currentHtml);
                    }
                } else if (maintenanceThreadViewModel.getTab().equals(DATE_TAB)) {

                    threadTreeNodeList = threadService.getLatestThreadNodes(app.getId(), maintenanceThreadViewModel.getCurrentPage(), 40);

                    String currentHtml = getEditThreadListHtml(threadTreeNodeList, maintenanceThreadViewModel.getCurrentPage(),
                            maintenanceThreadViewModel.getTab(), maintenanceThreadViewModel.isSearchSubmitted());
                    threadTreeHtml.add(currentHtml);
                }

                //if threads returned is less than asked for, there is no next page.
                if (threadTreeNodeList != null && threadTreeNodeList.size() < 20) {
                    maintenanceThreadViewModel.setHasNextPage(false);
                } else {
                    maintenanceThreadViewModel.setHasNextPage(true);
                }

                //todo : something wonky going on here. this is set here but the others are set in the above method..?
                //todo: setting above does not work.
                if (maintenanceThreadViewModel.isOnEditMessage()) {
                    Thread threadToEdit = threadService.getThread(app.getId(), maintenanceThreadViewModel.getEditArticleId());

                    if (threadToEdit != null && threadToEdit.getApplicationId().equals(app.getId())) {
                        ThreadTreeNode subThreads = threadService.getFullThreadTree(threadToEdit.getId());
                        if (subThreads != null) {
                            String subThreadHtml = getEditThreadHtml(subThreads, "", true, false,
                                    maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel.getTab(), maintenanceThreadViewModel.isSearchSubmitted());
                            maintenanceThreadViewModel.setEditArticleReplyThreadsHtml(subThreadHtml);
                        }
                    }
                }

                maintenanceThreadViewModel.setEditThreadTreeHtml(threadTreeHtml);
                maintenanceThreadViewModel.setNumberOfMessages(threadService.getTotalThreadCountForApplicationId(app.getId()));

            } else if (maintenanceThreadViewModel.getTab().equalsIgnoreCase(UNAPPROVED_TAB)) {
                //unapproved thread tab is selected.
                if (unapprovedThreads != null) {
                    String unapprovedThreadsResultHtml = getListThreadHtml(
                            unapprovedThreads, UNAPPROVED_TAB,
                            maintenanceThreadViewModel.isSearchSubmitted(),
                            maintenanceThreadViewModel.getAuthorSearch(),
                            maintenanceThreadViewModel.getEmailSearch(),
                            maintenanceThreadViewModel.getSubjectSearch(),
                            maintenanceThreadViewModel.getIpSearch(),
                            maintenanceThreadViewModel.getMessageSearch(),
                            maintenanceThreadViewModel.getApprovedSearch());
                    List<String> threadHtml = new ArrayList<>();
                    threadHtml.add(unapprovedThreadsResultHtml);
                    maintenanceThreadViewModel.setEditThreadTreeHtml(threadHtml);
                    maintenanceThreadViewModel.setNumberOfMessages(unapprovedThreads.size());
                }
            }
        } catch (Exception ex) {
            log.error("Error: " + ex.getMessage(), ex);
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
        }
        return new ModelAndView("admin/disc-edit", "maintenanceThreadViewModel", maintenanceThreadViewModel);
    }

    @GetMapping(THREAD_EDIT_PAGE)
    public ModelAndView getEditThreadView(@RequestParam(name = "id") long appId,
                                          @RequestParam(name = "article") long threadId,
                                          @RequestParam(name = "page", required = false) Integer page,
                                          @RequestParam(name = "tab", required = false) String tab,
                                          @RequestParam(name = "isSearching", required = false) Boolean isSearching,
                                          @RequestParam(name = "authorSearch", required = false) String authorSearch,
                                          @RequestParam(name = "emailSearch", required = false) String emailSearch,
                                          @RequestParam(name = "subjectSearch", required = false) String subjectSearch,
                                          @RequestParam(name = "ipSearch", required = false) String ipSearch,
                                          @RequestParam(name = "messageSearch", required = false) String messageSearch,
                                          @RequestParam(name = "approvedSearch", required = false) String approvedSearch,
                                          @ModelAttribute MaintenanceThreadViewModel maintenanceThreadViewModel,
                                          Model model,
                                          HttpServletResponse response) {

        try {
            Application app = applicationService.get(appId);
            String username = accountHelper.getLoggedInEmail();
            setCommonModelAttributes(model, app, username);
            maintenanceThreadViewModel.setCurrentPage(page);

            if (tab != null && !tab.isEmpty()) {
                maintenanceThreadViewModel.setTab(tab);
            }

            if (isSearching != null) {
                maintenanceThreadViewModel.setSearchSubmitted(isSearching);
            }

            maintenanceThreadViewModel.setOnEditMessage(true);

            Thread editingThread = threadService.getThread(app.getId(), threadId);
            if (editingThread != null) {
                maintenanceThreadViewModel.setEditArticleId(editingThread.getId());
                maintenanceThreadViewModel.setEditArticleParentId(editingThread.getParentId());
                maintenanceThreadViewModel.setEditArticleSubmitter(editingThread.getSubmitter());
                maintenanceThreadViewModel.setEditArticleEmail(editingThread.getEmail());
                maintenanceThreadViewModel.setEditArticleSubject(editingThread.getSubject());
                maintenanceThreadViewModel.setEditArticleCreateDt(editingThread.getCreateDt().toString());
                maintenanceThreadViewModel.setEditArticleModDt(editingThread.getModDt().toString());
                maintenanceThreadViewModel.setEditArticleIpAddress(editingThread.getIpAddress());
                maintenanceThreadViewModel.setEditArticleMessage(editingThread.getBody());
                maintenanceThreadViewModel.setEditArticleUserAgent(editingThread.getUserAgent());

                if (editingThread.getDiscAppUser() != null) {
                    maintenanceThreadViewModel.setEditArticleUserEmail(editingThread.getDiscAppUser().getEmail());
                    maintenanceThreadViewModel.setEditArticleCurrentUsername(editingThread.getDiscAppUser().getUsername());
                    maintenanceThreadViewModel.setEditArticleUserId(editingThread.getDiscAppUser().getId());
                }
            }
        } catch (Exception ex) {
            log.error("Error getting edit thread view: " + ex.getMessage(), ex);
        }

        return getDiscEditView(appId, maintenanceThreadViewModel.getTab(), maintenanceThreadViewModel.getCurrentPage(), maintenanceThreadViewModel, model, response);
    }


    /**
     * Generates edit thread HTML for threads view which is a stripped down version of the default view.
     *
     * @param currentNode Node to create list HTML for
     * @param currentHtml Current html to be built upon
     * @return Returns generated HTML
     */
    private String getEditThreadHtml(ThreadTreeNode currentNode, String currentHtml, boolean skipCurrent,
                                     boolean includeCheckBox, int currentPage, String tab, boolean isSearching) {

        if (!skipCurrent) {
            currentHtml +=
                    "<li>" +
                            "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() + "&amp;page=" + currentPage
                            + "&amp;tab=" + tab + "&amp;isSearching=" + isSearching + "\">" +
                            currentNode.getCurrent().getSubject() +
                            "</a> " +

                            "<label for=\"checkbox_" + currentNode.getCurrent().getId() + "\">" +
                            "    <span style=\"font-size:smaller;\">" +
                            "        <span style=\"font-style:italic; margin-left:1ex; margin-right:1ex;\">" +
                            currentNode.getCurrent().getSubmitter() + " " +
                            currentNode.getCurrent().getCreateDt() +
                            "        </span>" +
                            "    </span>" +
                            "</label>";
            if (includeCheckBox) {
                currentHtml += "<label>" +
                        "    <input type=\"checkbox\" name=\"selectThreadCheckbox\" value=\"" + currentNode.getCurrent().getId() +
                        "\" id=\"checkbox_" + currentNode.getCurrent().getId() + "\"/>" +
                        "</label>";
            }

            currentHtml += "</li>";

        }
        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getEditThreadHtml(node, currentHtml, false, includeCheckBox, currentPage, tab, isSearching);
            currentHtml += "</ul>";
        }

        return currentHtml;
    }

    /**
     * Gets HTML string for the edit thread view by date. Threads are sorted by date desc
     *
     * @param threadTreeNodeList ThreadTreeNode list to use to populate HTML string
     * @return Generated HTML for node list
     */
    private String getEditThreadListHtml(List<ThreadTreeNode> threadTreeNodeList, int currentPage, String tab, boolean isSearching) {

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
                            "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() + "&amp;page=" + currentPage
                            + "&amp;tab=" + tab + "&amp;isSearching=" + isSearching + "\">" +
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
     *
     * @param current            current ThreadTreeNode to start populating from (will traverse sub nodes)
     * @param threadTreeNodeList ThreadTreeNode list to add parent and sub nodes too from current node.
     */
    private void populateFlatThreadList(ThreadTreeNode current, List<ThreadTreeNode> threadTreeNodeList) {
        threadTreeNodeList.add(current);
        for (ThreadTreeNode threadTreeNode : current.getSubThreads()) {
            populateFlatThreadList(threadTreeNode, threadTreeNodeList);
        }
    }


    private String getListThreadHtml(List<Thread> threads, String tab, boolean isSearching,
                                     String authorSearch, String emailSearch, String subjectSearch, String ipSearch,
                                     String messageSearch, String approvedSearch) {
        String currentHtml = "<ul>";

         for (Thread thread : threads) {

            currentHtml += "<li>" +
                    "<a href=\"" + CONTROLLER_URL_DIRECTORY + "edit-thread.cgi?id=" + thread.getApplicationId() +
                    "&amp;article=" + thread.getId() +
                    "&amp;tab=" + tab + "&amp;isSearching=" + isSearching + "&amp;authorSearch=" + authorSearch
                    + "&amp;emailSearch=" + emailSearch + "&amp;subjectSearch=" + subjectSearch + "&amp;ipSearch="
                    + ipSearch + "&amp;messageSearch=" + messageSearch + "&amp;approvedSearch=" + approvedSearch + "\">" +
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
