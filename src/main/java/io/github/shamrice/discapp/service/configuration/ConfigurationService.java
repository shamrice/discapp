package io.github.shamrice.discapp.service.configuration;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.repository.ConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationService {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private ConfigurationRepository configurationRepository;

    public List<Configuration> list() {
        return configurationRepository.findAll();
    }
}
