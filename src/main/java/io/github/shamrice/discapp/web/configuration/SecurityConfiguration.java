package io.github.shamrice.discapp.web.configuration;

import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.account.UserPersistentLoginService;
import io.github.shamrice.discapp.web.filter.DiscAppIpBlockFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private UserPersistentLoginService userPersistentLoginService;

    @Value("${discapp.auth.remember-me.key:nediscapp_remember_me}")
    private String rememberMeKey;

    @Value("${discapp.auth.remember-me.token.duration:2592000}")
    private int rememberMeTokenDuration;

    @Value("${discapp.security.bcrypt.strength:15}")
    private int bcryptStrength;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {

        log.info("Using remember me key: " + rememberMeKey + " and duration of: " + rememberMeTokenDuration);

        //TODO : see https://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/html5/#csrf-include-csrf-token-ajax
        //TODO : _csrf token is not getting passed in the search function. One option is to include it, one to switch
        //TODO : search page to a GET mapping. Currently csrf is disabled and POST mappings are allowed

        //TODO: url paths need to be located in centralized location.
        httpSecurity
                .addFilterAfter(new DiscAppIpBlockFilter(), FilterSecurityInterceptor.class)
                .headers()
                    .frameOptions().disable()
                    .and()
                .csrf()
                    .disable()
                .authorizeRequests()
                    .antMatchers("/error/**").permitAll()
                    .antMatchers("/indices/**").permitAll()
                    .antMatchers("/Indices/**").permitAll()
                    .antMatchers("/styles/**").permitAll()
                    .antMatchers("/images/**").permitAll()
                    .antMatchers("/*").permitAll()
                    .antMatchers("/account/wizard").permitAll()
                    .antMatchers("/account/wizard/add").permitAll()
                    .antMatchers("/account/create").permitAll()
                    .antMatchers("/account/registration").permitAll()
                    .antMatchers("/account/password/**").permitAll()
                    .antMatchers("/account/password").permitAll()
                    .antMatchers("/password/reset/*").permitAll()
                    .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/abuse/**").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/account/delete").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/delete/status").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/modify").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/modify/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/add/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/site_admin/**").access("hasRole('ROLE_ROOT')")
                    .antMatchers("/auth/**").authenticated()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .successHandler(successHandler())
                    .failureHandler(failureHandler())
                    .and()
                .rememberMe()
                    .key(rememberMeKey)
                    .tokenRepository(userPersistentLoginService)
                    .rememberMeParameter("remember-me")
                    .rememberMeCookieName("nediscapp-remember-me")
                    .tokenValiditySeconds(rememberMeTokenDuration)
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(logoutSuccessHandler())
                    .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        if (bcryptStrength == 15) {
            log.info("Using default bcrypt strength value.");
        } else {
            log.info("Using custom bcrypt strength value.");
        }

        auth.userDetailsService(discAppUserDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder(bcryptStrength));
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomLoginSuccessHandler("/");
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return new CustomLoginFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

}
