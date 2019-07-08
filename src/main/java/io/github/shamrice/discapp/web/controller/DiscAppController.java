package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.ApplicationService;
import io.github.shamrice.discapp.service.ThreadService;
import io.github.shamrice.discapp.service.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.NewThreadViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DiscAppController {

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

                //List<Thread> threadList = threadService.getThreads(app.getId());
                //List<Thread> threadList = threadService.getFullThreadTree(1L);
                ThreadTreeNode threadTreeNode = threadService.getFullThreadTree(1L);
                //model.addAttribute("threadList", threadList);
                model.addAttribute("threadList", threadTreeNode);

                String testText = threadTreeNode.getCurrent().getSubject() + " " + threadTreeNode.getCurrent().getSubmitter() + " : ";
                for (ThreadTreeNode next : threadTreeNode.getSubThreads()) {
                    testText += next.getCurrent().getSubject() + " " + next.getCurrent().getSubmitter() + " : ";
                }

                model.addAttribute("testText", testText);

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
}
