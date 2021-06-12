package io.github.shamrice.discapp.exception;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import lombok.Getter;

public class DiscAppConfigurationException extends Exception {

    @Getter
    private final ConfigurationProperty configurationProperty;

    @Override
    public String toString() {
        return "DiscAppConfigurationException{" +
                "configurationProperty='" + configurationProperty.getPropName() + '\'' +
                "message='" + super.getMessage() + '\'' +
                '}';
    }

    public DiscAppConfigurationException(ConfigurationProperty configurationProperty, String message, Throwable cause) {
        super(message, cause);
        this.configurationProperty = configurationProperty;
    }

    public DiscAppConfigurationException(ConfigurationProperty configurationProperty, String message) {
        super(message);
        this.configurationProperty = configurationProperty;
    }

    @Override
    public String getMessage() {
        return "PropertyName: " + this.configurationProperty.getPropName() + " :: " + super.getMessage();
    }
}
