package io.github.shamrice.discapp.web.controller.abuse;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.ReportedAbuse;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.web.model.abuse.AbuseViewModel;
import io.github.shamrice.discapp.web.util.AccountHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.AbuseUrl.*;

@Slf4j
@Controller
public class AbuseController {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private DiscAppUserDetailsService userDetailsService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping(ABUSE_DELETE)
    public ModelAndView getAbuseView(@RequestParam(name = ABUSE_ID_QUERY_PARAM) Long abuseId,
                                     @RequestParam(name = DISC_APP_ID_QUERY_PARAM, required = false) Long appId,
                                     @RequestParam(name = SUBMITTER_QUERY_PARAM, required = false) String submitter,
                                     @RequestParam(name = EMAIL_QUERY_PARAM, required = false) String email,
                                     @RequestParam(name = IP_QUERY_PARAM, required = false) String ip,
                                     @RequestParam(name = SUBJECT_QUERY_PARAM, required = false) String subject,
                                     @RequestParam(name = BODY_QUERY_PARAM, required = false) String body,
                                     AbuseViewModel abuseViewModel,
                                     Model model) {
        ReportedAbuse reportedAbuse = threadService.getReportedAbuse(abuseId);

        if (reportedAbuse != null) {
            //make sure user owns app that they're trying to delete entry for.
            boolean isRootAdmin = accountHelper.isRootAdminAccount();
            String userEmail = accountHelper.getLoggedInEmail();
            DiscAppUser user = userDetailsService.getByEmail(userEmail);
            if (user != null && user.getOwnerId() != null) {
                boolean isOwnerFound = false;
                for (Application ownedApp : applicationService.getByOwnerId(user.getOwnerId())) {
                    if (ownedApp.getId().equals(reportedAbuse.getApplicationId())) {
                        //make sure it's a user account or the system account that matches the app id.
                        if (user.getIsUserAccount() || user.getEmail().equals(ownedApp.getId().toString())) {
                            log.info("User: " + userEmail + " has deleted reported abuse id: " + reportedAbuse.getId());
                            threadService.deleteReportedAbuse(reportedAbuse.getId());
                            abuseViewModel.setInfoMessage("Removed abuse record from database.");
                            isOwnerFound = true;
                            break;
                        }
                    }
                }
                if (!isOwnerFound) {
                    abuseViewModel.setErrorMessage("You cannot delete entries that you do not own.");
                    log.warn("User: " + userEmail + " attempted to delete reported abuse id: " + reportedAbuse.getId()
                            + " but they do not own it. Action has been blocked.");
                }
            } else if (isRootAdmin) {
                log.info("ROOT User has deleted reported abuse id: " + reportedAbuse.getId());
                threadService.deleteReportedAbuse(reportedAbuse.getId());
                abuseViewModel.setInfoMessage("Removed abuse record from database.");
            } else {
                abuseViewModel.setErrorMessage("You must be the owner to remove a reported abuse record.");
            }
        } else {
            abuseViewModel.setErrorMessage("Could not find record to remove.");
        }


        return getAbuseView(appId, submitter, email, ip, subject, body, abuseViewModel, model);
    }

    @GetMapping(ABUSE_VIEW)
    public ModelAndView getAbuseView(@RequestParam(name = ID_QUERY_PARAM, required = false) Long appId,
                                     @RequestParam(name = SUBMITTER_QUERY_PARAM, required = false) String submitter,
                                     @RequestParam(name = EMAIL_QUERY_PARAM, required = false) String email,
                                     @RequestParam(name = IP_QUERY_PARAM, required = false) String ip,
                                     @RequestParam(name = SUBJECT_QUERY_PARAM, required = false) String subject,
                                     @RequestParam(name = BODY_QUERY_PARAM, required = false) String body,
                                     AbuseViewModel abuseViewModel,
                                     Model model) {

        if (appId != null) {
            abuseViewModel.setDiscAppId(appId);
        }
        abuseViewModel.setSubmitter(submitter != null ? submitter : "");
        abuseViewModel.setEmail(email != null ? email : "");
        abuseViewModel.setIpAddress(ip != null ? ip : "");
        abuseViewModel.setSubject(subject != null ? subject : "");
        abuseViewModel.setMessage(body != null ? body : "");

        List<ReportedAbuse> reportedAbuses = threadService.searchForReportedAbuse(appId, submitter, email, ip, subject, body);

        //get list of apps owned by logged in user.
        List<Long> ownedAppIds = new ArrayList<>();
        String userEmail = accountHelper.getLoggedInEmail();
        DiscAppUser user = userDetailsService.getByEmail(userEmail);

        //generate list of owned applications that user can delete records for
        if (user != null && user.getOwnerId() != null) {
            List<Application> ownedApps = applicationService.getByOwnerId(user.getOwnerId());
            for (Application ownedApp : ownedApps) {
                //only add matching app id for system accounts.
                if (user.getIsUserAccount() || user.getEmail().equals(ownedApp.getId().toString())) {
                    ownedAppIds.add(ownedApp.getId());
                }
            }
        }

        boolean isRoot = accountHelper.isRootAdminAccount();

        for (ReportedAbuse reportedAbuse : reportedAbuses) {
            if (reportedAbuse.getThread() != null) {

                //if user owns app that abuse is reported for, flag it as deletable.
                boolean isDeletable = false;
                String deleteQueryParam = "";

                if (isRoot || ownedAppIds.contains(reportedAbuse.getApplicationId())) {
                    isDeletable = true;

                    deleteQueryParam = "?" + ABUSE_ID_QUERY_PARAM + "=" + reportedAbuse.getId()
                            + "&" + DISC_APP_ID_QUERY_PARAM + "=" + reportedAbuse.getApplicationId()
                            + "&" + SUBMITTER_QUERY_PARAM + "=" + abuseViewModel.getSubmitter()
                            + "&" + EMAIL_QUERY_PARAM + "=" + abuseViewModel.getEmail()
                            + "&" + IP_QUERY_PARAM + "=" + abuseViewModel.getIpAddress()
                            + "&" + SUBJECT_QUERY_PARAM + "=" + abuseViewModel.getSubject()
                            + "&" + BODY_QUERY_PARAM + "=" + abuseViewModel.getMessage();
                }

                AbuseViewModel.ReportedThread reportedThread = new AbuseViewModel.ReportedThread(
                        reportedAbuse.getApplicationId(),
                        reportedAbuse.getThread().getCreateDt(),
                        reportedAbuse.getThread().getIpAddress(),
                        reportedAbuse.getThread().getSubmitter(),
                        reportedAbuse.getThread().getEmail(),
                        reportedAbuse.getThread().getSubject(),
                        reportedAbuse.getThread().getId(),
                        isDeletable
                );
                if (isDeletable) {
                    reportedThread.setDeleteUrlQueryParameter(deleteQueryParam);
                }
                abuseViewModel.getReportedThreads().add(reportedThread);
            }
        }
        //set whois url based on site config
        abuseViewModel.setWhoIsUrl(configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.WHOIS_URL, "https://www.whois.com/whois/"));

        return new ModelAndView("abuse/abuse-results", "abuseViewModel", abuseViewModel);
    }

    @GetMapping(ABUSE_SEARCH)
    public ModelAndView getAbuseSearchView(AbuseViewModel abuseViewModel,
                                           Model model) {
        return new ModelAndView("abuse/abuse-search", "abuseViewModel", abuseViewModel);
    }

    @GetMapping(ABUSE_SEARCH_VIEW)
    public ModelAndView getAbuseSearchView(@RequestParam(name = ARTICLE_ID_QUERY_PARAM) long threadId,
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
