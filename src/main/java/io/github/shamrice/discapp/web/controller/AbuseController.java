package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.web.model.AbuseViewModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AbuseController {

    @GetMapping("/abuse/abuse.cgi")
    public ModelAndView getAbuseView(@RequestParam(name = "id", required = false) Long appId,
                                     AbuseViewModel abuseViewModel,
                                     Model model) {
        /*
        TODO : if appId is not null, view results for that appId.
        otherwise, results should be search for search results.
         */


        return new ModelAndView("abuse/abuse-results", "abuseViewModel", abuseViewModel);
    }

    @GetMapping("/abuse/abuse-search.cgi")
    public ModelAndView getAbuseSearchView(AbuseViewModel abuseViewModel,
                                           Model model) {
        return new ModelAndView("abuse/abuse-search", "abuseViewModel", abuseViewModel);
    }

    @GetMapping("/abuse/abuse-view.cgi")
    public ModelAndView getAbuseSearchView(@RequestParam(name = "id") long appId,
                                           @RequestParam(name = "articleId") long threadId,
                                           AbuseViewModel abuseViewModel,
                                           Model model) {
        //todo : populate view model with values from thread id that was passed in.
        return new ModelAndView("abuse/abuse-view", "abuseViewModel", abuseViewModel);
    }
}
