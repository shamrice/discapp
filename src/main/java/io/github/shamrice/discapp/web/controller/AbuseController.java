package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.data.model.ReportedAbuse;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.AbuseViewModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
public class AbuseController {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/abuse/abuse.cgi")
    public ModelAndView getAbuseView(@RequestParam(name = "id", required = false) Long appId,
                                     @RequestParam(name = "submitter", required = false) String submitter,
                                     @RequestParam(name = "email", required = false) String email,
                                     @RequestParam(name = "ip", required = false) String ip,
                                     @RequestParam(name = "subject", required = false) String subject,
                                     @RequestParam(name = "body", required = false) String body,
                                     AbuseViewModel abuseViewModel,
                                     Model model) {
        List<ReportedAbuse> reportedAbuses = threadService.searchForReportedAbuse(appId, submitter, email, ip, subject, body);

        for (ReportedAbuse reportedAbuse : reportedAbuses) {
            if (reportedAbuse.getThread() != null) {
                AbuseViewModel.ReportedThread reportedThread = new AbuseViewModel.ReportedThread(
                        reportedAbuse.getApplicationId(),
                        reportedAbuse.getThread().getCreateDt(),
                        reportedAbuse.getThread().getIpAddress(),
                        reportedAbuse.getThread().getSubmitter(),
                        reportedAbuse.getThread().getEmail(),
                        reportedAbuse.getThread().getSubject(),
                        reportedAbuse.getThread().getId()
                );
                abuseViewModel.getReportedThreads().add(reportedThread);
            }
        }
        //set whois url based on site config
        abuseViewModel.setWhoIsUrl(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.WHOIS_URL, "https://www.whois.com/whois/"));

        return new ModelAndView("abuse/abuse-results", "abuseViewModel", abuseViewModel);
    }

    @GetMapping("/abuse/abuse-search.cgi")
    public ModelAndView getAbuseSearchView(AbuseViewModel abuseViewModel,
                                           Model model) {
        return new ModelAndView("abuse/abuse-search", "abuseViewModel", abuseViewModel);
    }

    @GetMapping("/abuse/abuse-view.cgi")
    public ModelAndView getAbuseSearchView(@RequestParam(name = "articleId") long threadId,
                                           AbuseViewModel abuseViewModel,
                                           Model model) {
         Thread thread = threadService.getThreadById(threadId);
         if (thread != null) {
             abuseViewModel.setDiscAppId(thread.getApplicationId());
             abuseViewModel.setThreadId(thread.getId());
             abuseViewModel.setSubmitter(thread.getSubmitter());
             abuseViewModel.setEmail(thread.getEmail());
             abuseViewModel.setIpAddress(thread.getIpAddress());
             abuseViewModel.setCreateDt(thread.getCreateDt());
             abuseViewModel.setSubject(thread.getSubject());

             if (thread.getBody() != null) {
                 abuseViewModel.setMessage(thread.getBody());
             }
         } else {
             log.warn("No abuse thread found for thread Id: " + threadId);
             abuseViewModel.setErrorMessage("Failed to find reported thread with id: " + threadId);
         }

        //set whois url based on site config
        abuseViewModel.setWhoIsUrl(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.WHOIS_URL, "https://www.whois.com/whois/"));

        return new ModelAndView("abuse/abuse-view", "abuseViewModel", abuseViewModel);
    }
}
