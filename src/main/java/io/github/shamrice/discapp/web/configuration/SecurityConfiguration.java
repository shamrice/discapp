package io.github.shamrice.discapp.web.configuration;

import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {

        //TODO : see https://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/html5/#csrf-include-csrf-token-ajax
        //TODO : _csrf token is not getting passed in the search function. One option is to include it, one to switch
        //TODO : search page to a GET mapping. Currently csrf is disabled and POST mappings are allowed

        //TODO: url paths need to be located in centralized location.
        httpSecurity
                .headers()
                    .frameOptions().disable()
                    .and()
                .csrf()
                    .disable()
                .authorizeRequests()
                    .antMatchers("/indices/**").permitAll()
                    .antMatchers("/Indices/**").permitAll()
                    .antMatchers("/styles/**").permitAll()
                    .antMatchers("/images/**").permitAll()
                    .antMatchers("/*").permitAll()
                    .antMatchers("/account/create").permitAll()
                    .antMatchers("/account/password/**").permitAll()
                    .antMatchers("/account/password").permitAll()
                    .antMatchers("/password/reset/*").permitAll()
                    .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/account/delete").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/delete/status").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/modify").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/modify/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/account/add/**").access("hasRole('ROLE_USER')")
                    .antMatchers("/test/test").authenticated()
                    .antMatchers("/auth/**").authenticated()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .successHandler(successHandler())
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(logoutSuccessHandler())
                    .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(discAppUserDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder(15));
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomLoginSuccessHandler("/");
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }
}
