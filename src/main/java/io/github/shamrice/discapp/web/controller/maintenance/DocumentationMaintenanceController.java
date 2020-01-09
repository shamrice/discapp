package io.github.shamrice.discapp.web.controller.maintenance;


import io.github.shamrice.discapp.data.model.Application;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static io.github.shamrice.discapp.web.define.url.MaintenanceUrl.DOCUMENTATION_URL;

@Controller
public class DocumentationMaintenanceController extends MaintenanceController {


    @GetMapping(DOCUMENTATION_URL)
    public ModelAndView getDocumentationMaintenanceView(@RequestParam(name = "id") long appId,
                                                        HttpServletRequest request,
                                                        Model model) {

        Application app = applicationService.get(appId);
        String username = accountHelper.getLoggedInEmail();

        setCommonModelAttributes(model, app, username);

        String baseUrl = webHelper.getBaseUrl(request);

        String searchHtml = "" +
                "    <b>Search the message board:</b>\n" +
                "    <FORM METHOD=\"POST\" ACTION=\"" + baseUrl + "/indices/search?disc=" + appId +"\">\n" +
                "        <INPUT TYPE=\"hidden\" NAME=\"id\" VALUE=\"" + appId + "\">\n" +
                "        <INPUT TYPE=\"text\" NAME=\"searchTerm\">\n" +
                "        <INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Search\">\n" +
                "    </FORM>";

        model.addAttribute("searchHtml", searchHtml);

        String adminLinkHtml = "" +
                "    <a href=\"" + baseUrl + "/admin/disc-maint.cgi?id=" + appId + "\">Admin</a>";

        model.addAttribute("adminLinkHtml", adminLinkHtml);

        return new ModelAndView("admin/disc-docs", "docModel", model);
    }

}

