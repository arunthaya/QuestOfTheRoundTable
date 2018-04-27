package com.devglan.config;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.devglan.config.Application.logIt;

@Controller
@RequestMapping(value = "/js-log")
public class JSLogger {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logError(final HttpServletRequest request, @RequestBody(required = true) final String logMessage) {
        logIt.warn("Received client-side logmessage (" +logMessage);
    }

}
