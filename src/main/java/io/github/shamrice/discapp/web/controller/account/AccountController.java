package io.github.shamrice.discapp.web.controller.account;

import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.thread.ThreadService;
import io.github.shamrice.discapp.service.thread.UserReadThreadService;
import io.github.shamrice.discapp.web.util.AccountHelper;
import io.github.shamrice.discapp.web.util.InputHelper;
import io.github.shamrice.discapp.web.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AccountController {

    protected static final String ENABLED = "enabled";
    protected static final String DISABLED = "disabled";
    protected static final String DELETE = "delete";

    @Autowired
    protected DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected ConfigurationService configurationService;

    @Autowired
    protected ThreadService threadService;

    @Autowired
    protected UserReadThreadService userReadThreadService;

    @Autowired
    protected AccountHelper accountHelper;

    @Autowired
    protected InputHelper inputHelper;

    @Autowired
    protected WebHelper webHelper;

}
