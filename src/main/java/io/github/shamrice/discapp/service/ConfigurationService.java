package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Configuration;
import io.github.shamrice.discapp.data.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    public List<Configuration> list() {
        return configurationRepository.findAll();
    }
}
