package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.service.application.ApplicationService;
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

import java.util.ArrayList;
import java.util.List;

@Controller
public class DiscAppController {

    private static Logger logger = LoggerFactory.getLogger(DiscAppController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ThreadService threadService;

    @GetMapping("/indices/{applicationId}")
    public String getAppView(@PathVariable(name = "applicationId") Long appId, Model model) {

        try {
            //Long id = Long.parseLong(appId);

            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());

                //model.addAttribute("newthread", new NewThreadViewModel()); //TODO : remove this

                model.addAttribute("prologueText", applicationService.getPrologueText(app.getId()));
                model.addAttribute("epilogueText", applicationService.getEpilogueText(app.getId()));

                //TODO : limit should be pulled from database configuration
                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), 10);
                List<String> threadTreeHtml = new ArrayList<>();

                for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                    threadTreeHtml.add(getAppViewThreadHtml(threadTreeNode, "<ul>", false) + "</ul>");
                }

                model.addAttribute("threadNodeList", threadTreeHtml);

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
                logger.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
            logger.error("Error getting disc app with id of " + appId + ". Returning null. ", ex.getMessage());
        }

        return "indices/appView";
    }

    @GetMapping("/createThread")
    public String createNewThread(@RequestParam(name = "disc") String appId,
                                Model model) {
        model.addAttribute("appId", appId);
        model.addAttribute("newthread", new NewThreadViewModel());
        return "indices/createThread";
    }

    @PostMapping("/postThread")
    public String postNewThread(@RequestParam(name = "disc") Long appId,
                                @ModelAttribute NewThreadViewModel newThreadViewModel,
                                Model model) {
        if (newThreadViewModel != null) {
            logger.info("new thread: " + newThreadViewModel.getAppId() + " : " + newThreadViewModel.getSubmitter() + " : "
                    + newThreadViewModel.getSubject() + " : " + newThreadViewModel.getBody());
            //TODO : create thread in db;
        }
        //TODO: if success only.
        return getAppView(appId, model);
    }

    @GetMapping("discussion.cgi")
    public String getViewThread(@RequestParam(name = "disc") Long appId,
                                @RequestParam(name = "article") Long threadId,
                                @RequestParam(name = "title", required = false) String pageTitle,
                                Model model) {

        logger.info("Getting thread id " + threadId + " for app id: " + appId + " with title : " + pageTitle);

        Thread currentThread = threadService.getThread(threadId);
        if (currentThread != null) {
            ThreadTreeNode subThreadNode = threadService.getFullThreadTree(currentThread.getId());

            List<String> subThreadsHtml = new ArrayList<>();
            if (subThreadNode != null) {
                subThreadsHtml.add(getAppViewThreadHtml(subThreadNode, "", true) + "</ul>");
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

            model.addAttribute("threadViewModel", threadViewModel);
            model.addAttribute("subThreadsHtml", subThreadsHtml);

        }

        return "indices/viewThread";
    }

    @PostMapping("discussion.cgi")
    public String postDiscussionForm(@RequestParam(name = "disc") Long appId,
                                Model model) {

        return getAppView(appId, model);
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
    private String getAppViewThreadHtml(ThreadTreeNode currentNode, String currentHtml, boolean skipCurrentNode) {

        if (!skipCurrentNode) {
            currentHtml += " <li>"
                    + " <div class=\"first_message_div\">"
                    + " <div class=\"first_message_header\">"
                    + "    <span class=\"first_message_span\">"
                    + "        <a class=\"article_link\""
                    + " href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId()
                    + "&article=" + currentNode.getCurrent().getId() + "&title=N.E.M.B.%20\" name=\"21011\">"
                    + currentNode.getCurrent().getSubject()
                    + "        </a> "
                    + "        &#9787; " //todo: separator comes from app config in db
                    + "        <span class=\"author_cell\"> " + currentNode.getCurrent().getSubmitter() + ",</span> "
                    + "        <span class=\"date_cell\"> " + currentNode.getCurrent().getCreateDt() + "</span> "
                    + "    </span> "
                    + "</div> "
                    + "</div>";
        }
        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getAppViewThreadHtml(node, currentHtml, false);
            currentHtml += "</ul>";
        }

        currentHtml += " </li>";

        return currentHtml;
    }
}
