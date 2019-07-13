package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.NewThreadViewModel;
import io.github.shamrice.discapp.web.model.ThreadViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class DiscAppController {

    private static Logger logger = LoggerFactory.getLogger(DiscAppController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/indices/{applicationId}")
    public String getAppView(@PathVariable(name = "applicationId") Long appId, Model model) {

        try {
            //Long id = Long.parseLong(appId);

            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                model.addAttribute("prologueText", applicationService.getPrologueText(app.getId()));
                model.addAttribute("epilogueText", applicationService.getEpilogueText(app.getId()));

                model.addAttribute("postMessageButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));

                //TODO : limit should be pulled from database configuration
                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), 10);
                List<String> threadTreeHtml = new ArrayList<>();
                String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");

                for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                    threadTreeHtml.add(getAppViewThreadHtml(threadTreeNode, "<ul>", entryBreakString, false) + "</ul>");
                }

                model.addAttribute("threadNodeList", threadTreeHtml);
                model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
                logger.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            logger.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return "indices/appView";
    }

    @GetMapping("/createThread")
    public String createNewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "parent", required = false) Long parentThreadId,
                                Model model) {
        Application app = applicationService.get(appId);

        if (parentThreadId == null) {
            parentThreadId = 0L;
        }

        model.addAttribute("appName", app.getName());
        model.addAttribute("appId", appId);
        model.addAttribute("parentThreadId", parentThreadId);
        model.addAttribute("newthread", new NewThreadViewModel());

        model.addAttribute("submitterLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
        model.addAttribute("emailLabel", configurationService.getStringValue(appId, ConfigurationProperty.EMAIL_LABEL_TEXT, "Email:"));
        model.addAttribute("subjectLabel", configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));
        model.addAttribute("bodyLabel", configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BODY_LABEL_TEXT, "Message Text:"));
        model.addAttribute("previewButtonText", configurationService.getStringValue(appId, ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview"));
        model.addAttribute("postButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message"));
        model.addAttribute("returnButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages"));

        model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
        model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));

        return "indices/createThread";
    }

    @PostMapping("/postThread")
    public RedirectView postNewThread(@RequestParam(name = "disc") Long appId,
                                @ModelAttribute NewThreadViewModel newThreadViewModel,
                                Model model) {
        if (newThreadViewModel != null) {

            if (newThreadViewModel.getReturnToApp() != null && !newThreadViewModel.getReturnToApp().isEmpty()) {
                logger.info("Return to app button clicked for app id " + appId + ". Value=" + newThreadViewModel.getReturnToApp());
                return new RedirectView("/indices/" + appId);
            } else if (newThreadViewModel.getSubmitNewThread() != null && !newThreadViewModel.getSubmitNewThread().isEmpty()
                    && newThreadViewModel.getSubmitter() != null && !newThreadViewModel.getSubmitter().isEmpty()
                    && newThreadViewModel.getSubject() != null && !newThreadViewModel.getSubject().isEmpty()) {

                logger.info("new thread: " + newThreadViewModel.getAppId() + " : " + newThreadViewModel.getSubmitter() + " : "
                        + newThreadViewModel.getSubject() + " : " + newThreadViewModel.getBody());

                Thread newThread = new Thread();
                newThread.setApplicationId(appId);
                newThread.setParentId(Long.parseLong(newThreadViewModel.getParentId()));
                newThread.setDeleted(false);
                newThread.setCreateDt(new Date());
                newThread.setModDt(new Date());
                newThread.setSubject(newThreadViewModel.getSubject());
                newThread.setSubmitter(newThreadViewModel.getSubmitter());

                threadService.createNewThread(newThread, newThreadViewModel.getBody());
            }
        }
        logger.info("Error posting thread or couldn't find redirect action for POST. Fallback return to thread view.");
        return new RedirectView("/indices/" + appId);
    }

    @GetMapping("discussion.cgi")
    public String getViewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "article") Long threadId,
                                Model model) {

        logger.info("Getting thread id " + threadId + " for app id: " + appId);

        Thread currentThread = threadService.getThread(threadId);
        if (currentThread != null) {
            ThreadTreeNode subThreadNode = threadService.getFullThreadTree(currentThread.getId());

            List<String> subThreadsHtml = new ArrayList<>();
            if (subThreadNode != null) {
                String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                subThreadsHtml.add(getAppViewThreadHtml(subThreadNode, "", entryBreakString, true) + "</ul>");
            }

            String threadBody = threadService.getThreadBodyText(threadId);

            ThreadViewModel threadViewModel = new ThreadViewModel();
            threadViewModel.setBody(threadBody);
            threadViewModel.setCreateDt(currentThread.getCreateDt().toString());
            threadViewModel.setModDt(currentThread.getModDt().toString());
            threadViewModel.setAppId(appId.toString());
            threadViewModel.setId(threadId.toString());
            threadViewModel.setIpAddress(currentThread.getIpAddress());
            threadViewModel.setParentId(currentThread.getParentId().toString());
            threadViewModel.setSubject(currentThread.getSubject());
            threadViewModel.setSubmitter(currentThread.getSubmitter());

            Application app = applicationService.get(appId);
            model.addAttribute("appName", app.getName());

            model.addAttribute("threadViewModel", threadViewModel);
            model.addAttribute("subThreadsHtml", subThreadsHtml);

            model.addAttribute("replyButtonText", configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply"));
            model.addAttribute("returnButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages"));
            model.addAttribute("shareButtonText", configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Share"));

            model.addAttribute("headerText", configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
            model.addAttribute("footerText", configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
        }

        return "indices/viewThread";
    }

    @PostMapping("discussion.cgi")
    public RedirectView postDiscussionForm(@RequestParam(name = "disc") Long appId,
                                           ThreadViewModel threadViewModel,
                                           Model model) {
        if (threadViewModel != null) {
            if (threadViewModel.getReturnToApp() != null && !threadViewModel.getReturnToApp().isEmpty()) {
                logger.info("Return to app button clicked for app id " + appId + ". Value=" + threadViewModel.getReturnToApp());
                return new RedirectView("/indices/" + appId + "#" + threadViewModel.getId());
            } else if (threadViewModel.getPostResponse() != null && !threadViewModel.getPostResponse().isEmpty()) {
                logger.info("new reply appId: " + threadViewModel.getAppId() + " parent id : " + threadViewModel.getId()
                        + " submitter: " + threadViewModel.getSubmitter() + " : subject: "
                        + threadViewModel.getSubject() + " : email: " + threadViewModel.getEmail()
                        + " : body: " + threadViewModel.getBody());

                return new RedirectView("/createThread?disc=" + appId + "&parent=" + threadViewModel.getId());
            }
        }

        logger.info("Fallback return to thread view.");
        return new RedirectView("/indices/" + appId + "#" + threadViewModel.getId());
    }

    @GetMapping("/styles/disc_{applicationId}.css")
    public String getAppStyleSheet(@PathVariable(name = "applicationId") String appId) {
        return "styles/disc_" + appId + ".css";
    }


    /**
     * Recursive function that builds HTML for each thread in the app.
     * @param currentNode Current node being built. When calling, top level node is passed.
     * @param currentHtml Current html string. This is the the string that is built and returned
     * @return built HTML list structure for thread
     */
    private String getAppViewThreadHtml(ThreadTreeNode currentNode, String currentHtml, String entryBreakString, boolean skipCurrentNode) {


        if (!skipCurrentNode) {
            currentHtml += " <li>"
                    + " <div class=\"first_message_div\">"
                    + " <div class=\"first_message_header\">"
                    + "    <span class=\"first_message_span\">"
                    + "        <a class=\"article_link\""
                    + " href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId()
                    + "&article=" + currentNode.getCurrent().getId() + "\""
                    + " name=\"" + currentNode.getCurrent().getId() + "\">"
                    + currentNode.getCurrent().getSubject()
                    + "        </a>  "
                    + "        " + entryBreakString
                    + "        <span class=\"author_cell\"> " + currentNode.getCurrent().getSubmitter() + ",</span> "
                    + "        <span class=\"date_cell\"> " + currentNode.getCurrent().getCreateDt() + "</span> "
                    + "    </span> "
                    + "</div> "
                    + "</div>";
        }
        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getAppViewThreadHtml(node, currentHtml, entryBreakString, false);
            currentHtml += "</ul>";
        }

        currentHtml += " </li>";

        return currentHtml;
    }
}
