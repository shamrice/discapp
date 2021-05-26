package io.github.shamrice.discapp.service.notification.email.type;

import io.github.shamrice.discapp.service.notification.NotificationType;
import lombok.*;

import java.util.Map;

@Getter
@RequiredArgsConstructor
@ToString
public class TemplateEmail {

    @NonNull
    private final String to;

    @NonNull
    private final NotificationType notificationType;

    @Setter
    private Map<String, Object> subjectTemplateParams;

    @NonNull
    private final Map<String, Object> bodyTemplateParams;

    @NonNull
    private final boolean isMimeMessage;
}
