package io.github.shamrice.discapp.service.configuration;

public enum UserConfigurationProperty {

    THREAD_READ_TRACKING_ENABLED ("thread.read.tracking.enabled"),
    USER_TIMEZONE_ENABLED ("user.timezone.enabled"),
    USER_TIMEZONE_LOCATION("user.timezone.location"),
    USER_REPLY_NOTIFICATION_ENABLED("user.notification.reply.enabled");

    private final String propName;

    UserConfigurationProperty(String propName) {
        this.propName = propName;
    }

    public String getPropName() {
        return propName;
    }
}
