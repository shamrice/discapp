package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.ApplicationService;
import io.github.shamrice.discapp.service.ThreadService;
import io.github.shamrice.discapp.service.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.NewThreadViewModel;
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
    public String getAppView(@PathVariable(name = "applicationId") String appId, Model model) {

        try {
            Long id = Long.parseLong(appId);

            Application app = applicationService.get(id);

            if (app != null) {
                String appStr = "Id: " + app.getId() + " Name: " + app.getName()
                        + " owner_id: " + app.getOwnerId()
                        + " create: " + app.getCreateDt() + " mod: " + app.getModDt();

                model.addAttribute("appName", app.getName());
                model.addAttribute("appId", app.getId());
                model.addAttribute("appInfo", appStr);
                model.addAttribute("newthread", new NewThreadViewModel()); //TODO : remove this

                List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), 10);
                List<String> threadTreeHtml = new ArrayList<>();

                for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {
                    threadTreeHtml.add(getAppViewThreadHtml(threadTreeNode, "<ul>") + "</ul>");
                }

                model.addAttribute("threadNodeList", threadTreeHtml);

            } else {
                model.addAttribute("error", "Disc app with id " + appId + " returned null.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", "No disc app with id " + appId + " found. " + ex.getMessage());
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
    public String postNewThread(@RequestParam(name = "disc") String appId,
                                @ModelAttribute NewThreadViewModel newThreadViewModel,
                                Model model) {
        if (newThreadViewModel != null) {
            System.out.println("new thread: " + newThreadViewModel.getAppId() + " : " + newThreadViewModel.getSubmitter() + " : "
                    + newThreadViewModel.getSubject() + " : " + newThreadViewModel.getBody());
            //TODO : create thread in db;
        }
        //TODO: if success only.
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
    private String getAppViewThreadHtml(ThreadTreeNode currentNode, String currentHtml) {
        currentHtml += " <li>           <div class=\"first_message_div\">" +
                "<div class=\"first_message_header\">" +
                "        <span class=\"first_message_span\">" +
                "            <a class=\"article_link\"" +
        " href=\"/discussion.cgi?disc=46108;article=21011;title=N.E.M.B.%20\" name=\"21011\">" +
            currentNode.getCurrent().getSubject() +
        "                    </a> " +
        "        &#9787; " +
        "                    <span class=\"author_cell\"> " + currentNode.getCurrent().getSubmitter() + ",</span> " +
        "                    <span class=\"date_cell\"> " + currentNode.getCurrent().getCreateDt() + "</span> " +
        "                </span> " +
        "        </div> " +
        "    </div>";

        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {
            currentHtml += "<ul>";
            currentHtml = getAppViewThreadHtml(node, currentHtml);
            currentHtml += "</ul>";
        }

        currentHtml += " </li>";

        return currentHtml;
    }
}
