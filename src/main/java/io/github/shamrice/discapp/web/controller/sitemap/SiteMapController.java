package io.github.shamrice.discapp.web.controller.sitemap;

import io.github.shamrice.discapp.service.sitemap.GenericSiteMap;
import io.github.shamrice.discapp.service.sitemap.GenericSiteMapItem;
import io.github.shamrice.discapp.service.sitemap.SiteMapService;
import io.github.shamrice.discapp.web.model.sitemap.SiteMapModel;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static io.github.shamrice.discapp.web.define.url.SiteMapUrl.*;

@Controller
@Slf4j
public class SiteMapController {

    //Site map protocol: https://www.sitemaps.org/protocol.html
    //XSD Spec: https://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd

    @Autowired
    private SiteMapService siteMapService;

    @Autowired
    private WebHelper webHelper;

    @GetMapping(LATEST_ARTICLES_SITE_MAP_URL)
    @RequestMapping(value = LATEST_ARTICLES_SITE_MAP_URL, method = RequestMethod.GET, produces = "application/xml")
    public ModelAndView getLatestArticlesSiteMap(HttpServletRequest request) {

        log.info("Site map for latest articles requested.");
        String baseUrl = webHelper.getBaseUrl(request);

        SiteMapModel siteMapModel = new SiteMapModel();
        GenericSiteMap siteMap = siteMapService.getLatestGenericSiteMap();

        List<SiteMapModel.SiteMapItem> siteMapItems = new ArrayList<>();
        for (GenericSiteMapItem item : siteMap.getGenericSiteMapItems()) {
            SiteMapModel.SiteMapItem siteMapItem = new SiteMapModel.SiteMapItem(
                    baseUrl + item.getArticleUrl(),
                    item.getLastModified()
            );
            siteMapItems.add(siteMapItem);
        }

        siteMapModel.setSiteMapItems(siteMapItems);

        return new ModelAndView("sitemap/genericSiteMap", "siteMapModel", siteMapModel);
    }

    @GetMapping(FORUMS_SITE_MAP_URL)
    @RequestMapping(value = FORUMS_SITE_MAP_URL, method = RequestMethod.GET, produces = "application/xml")
    public ModelAndView getForumsSiteMap(HttpServletRequest request) {

        log.info("Site map for latest forum updates requested.");

        String baseUrl = webHelper.getBaseUrl(request);
        SiteMapModel siteMapModel = new SiteMapModel();
        GenericSiteMap siteMap = siteMapService.getLatestGenericSiteMap();

        List<String> addedForumUrls = new ArrayList<>();

        List<SiteMapModel.SiteMapItem> siteMapItems = new ArrayList<>();
        for (GenericSiteMapItem item : siteMap.getGenericSiteMapItems()) {

            //only add forum urls to site map once.
            if (!addedForumUrls.contains(item.getForumUrl())) {
                addedForumUrls.add(item.getForumUrl());
                SiteMapModel.SiteMapItem siteMapItem = new SiteMapModel.SiteMapItem(
                        baseUrl + item.getForumUrl(),
                        item.getLastModified()
                );
                siteMapItems.add(siteMapItem);

            }
        }

        siteMapModel.setSiteMapItems(siteMapItems);
        return new ModelAndView("sitemap/genericSiteMap", "siteMapModel", siteMapModel);
    }
}
