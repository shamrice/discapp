package io.github.shamrice.discapp.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    private static final String STATUS_CODE_ATTRIBUTE_NAME = "javax.servlet.error.status_code";
    private static final String ERROR_EXCEPTION_ATTRIBUTE_NAME = "javax.servlet.error.exception";
    private static final String ATTEMPTED_URI_ATTRIBUTE_NAME = "javax.servlet.error.request_uri";

    @RequestMapping(value = "/error", produces = "text/html")
    public ModelAndView getErrorView(HttpServletRequest request, Model model) {

        Integer statusCode = (Integer) request.getAttribute(STATUS_CODE_ATTRIBUTE_NAME);
        Exception exception = (Exception) request.getAttribute(ERROR_EXCEPTION_ATTRIBUTE_NAME);
        String attemptedUri = (String) request.getAttribute(ATTEMPTED_URI_ATTRIBUTE_NAME);


        String errorText = "";
        if (exception != null) {
            errorText = exception.getMessage();
        }


        if (statusCode.equals(HttpStatus.FORBIDDEN.value())) {
            logger.info("Status code 403 " + errorText, exception);
            return new ModelAndView("redirect:/login?relogin=true&" + request.getQueryString());
        }

        logger.error("Error: status code: " + statusCode + " : " + errorText + " :: attempted uri: "
                + attemptedUri, exception);

        model.addAttribute("errorStatusCode", statusCode);
        model.addAttribute("errorText", errorText);

        return new ModelAndView("error/error", "model", model);

    }

    @RequestMapping(value = "/error/notfound")
    public String getNotFoundView(String errorText, Model model) {
        model.addAttribute("errorText", errorText);
        return "error/notFound";
    }

    public String getErrorPath() {
        return "/error";
    }

}
