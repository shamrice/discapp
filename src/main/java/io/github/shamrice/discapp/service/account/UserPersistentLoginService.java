package io.github.shamrice.discapp.service.account;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import io.github.shamrice.discapp.data.model.UserPersistentLogin;
import io.github.shamrice.discapp.data.repository.UserPersistentLoginRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class UserPersistentLoginService implements PersistentTokenRepository {

    @Autowired
    private UserPersistentLoginRepository userPersistentLoginRepository;

    @Autowired
    private DiscAppUserDetailsService userDetailsService;

    @Override
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken) {
        if (persistentRememberMeToken != null) {
            UserPersistentLogin persistentLogin = new UserPersistentLogin();

            DiscAppUser user = userDetailsService.getByUsername(persistentRememberMeToken.getUsername());
            if (user != null) {
                persistentLogin.setUsername(user.getEmail());
                persistentLogin.setSeries(persistentRememberMeToken.getSeries());
                persistentLogin.setToken(persistentRememberMeToken.getTokenValue());
                persistentLogin.setLastUsed(persistentRememberMeToken.getDate());

                userPersistentLoginRepository.save(persistentLogin);
                log.info("Created new persistent login for user: " + user.getEmail() + " :: " + persistentLogin.toString());
            }
        }
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        UserPersistentLogin persistentLogin = userPersistentLoginRepository.findById(series).orElse(null);
        if (persistentLogin != null) {
            persistentLogin.setToken(tokenValue);
            persistentLogin.setLastUsed(lastUsed);
            userPersistentLoginRepository.save(persistentLogin);
            log.info("Updated persisted login :: " + persistentLogin.toString());
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        UserPersistentLogin persistentLogin = userPersistentLoginRepository.findById(series).orElse(null);
        if (persistentLogin != null) {
            log.info("Get token for series found: " + persistentLogin.toString());
            return new PersistentRememberMeToken(persistentLogin.getUsername(), persistentLogin.getSeries(), persistentLogin.getToken(), persistentLogin.getLastUsed());
        }
        log.info("Unable to find persistent token for series: " + series);
        return null;
    }

    @Override
    @Transactional
    public void removeUserTokens(String username) {
        DiscAppUser user = userDetailsService.getByUsername(username);
        if (user != null) {
            userPersistentLoginRepository.deleteByUsername(user.getEmail());
            log.info("Removed user tokens for user: " + user.getEmail());
        }
    }
}
