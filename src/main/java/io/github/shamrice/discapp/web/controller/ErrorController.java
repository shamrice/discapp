package io.github.shamrice.discapp.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private Logger logger = LoggerFactory.getLogger(ErrorController.class);


    @RequestMapping(value = "/error", produces = "text/html")
    public String getErrorView(HttpServletRequest request, Model model) {

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        logger.error("Error: status code: " + statusCode + " : " + exception.getMessage(), exception);

        model.addAttribute("errorStatusCode", statusCode);
        model.addAttribute("errorText", exception.getMessage());
        return "error/error";

    }

    public String getErrorPath() {
        return "/error";
    }

}
