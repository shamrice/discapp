package io.github.shamrice.discapp.web.controller.rss;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.application.ApplicationSubscriptionService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.enums.RssBehavior;
import io.github.shamrice.discapp.service.rss.RssService;
import io.github.shamrice.discapp.web.model.rss.RssViewModel;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.RssUrl.DISCUSSION_RSS_URL;

@Controller
@Slf4j
public class RssController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RssService rssService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private WebHelper webHelper;

    @GetMapping(DISCUSSION_RSS_URL)
    public ModelAndView getRssDiscussionFeed(@RequestParam(name="id")long appId,
                                             RssViewModel rssViewModel,
                                             HttpServletRequest request) {

        rssViewModel = new RssViewModel();
        Application app = applicationService.get(appId);
        if (app != null) {
            rssViewModel.setAppId(app.getId());
            rssViewModel.setAppName(app.getName());

            String baseUrl = webHelper.getBaseUrl(request);
            rssViewModel.setAppUrl(baseUrl + "/Indices/" + app.getId() + ".html");
            rssViewModel.setFeedUrl(baseUrl + DISCUSSION_RSS_URL + "?id=" + appId);

            String threadUrl = baseUrl + "/discussion.cgi?disc=" + appId + "&article=";

            String rssBehaviorStr = configurationService.getStringValue(appId, ConfigurationProperty.RSS_BEHAVIOR, "ALL");
            RssBehavior rssBehavior = RssBehavior.valueOf(rssBehaviorStr);

            List<RssViewModel.RssItem> rssItems = new ArrayList<>();
            List<Thread> rssThreads = rssService.getLatestThreadsForRssFeed(appId, rssBehavior);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            for (Thread thread : rssThreads) {
                String title = thread.getSubject() + " (" + thread.getSubmitter() + ")";
                String pubDate = dateFormat.format(thread.getCreateDt());

                RssViewModel.RssItem rssItem = new RssViewModel.RssItem(
                        title,
                        threadUrl + thread.getId(),
                        pubDate);

                if (RssBehavior.ALL_PREVIEW == rssBehavior || RssBehavior.FIRST_PREVIEW == rssBehavior) {
                    String preview = thread.getBody();
                    if (preview != null && preview.length() > 310) {
                        preview = preview.substring(0, 300) + "...";
                    }
                    rssItem.setDescription(preview);
                }
                rssItems.add(rssItem);
            }

            rssViewModel.setRssItems(rssItems);
        }

        return new ModelAndView("rss/discussion", "rssViewModel", rssViewModel);
    }
}
