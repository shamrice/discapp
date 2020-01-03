package io.github.shamrice.discapp.service.utility;

import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ReplyNotification {
    private @NonNull long appId;
    private @NonNull String appName;
    private @NonNull String discussionUrl;
    private @NonNull String emailAddress;
    private @NonNull long newThreadId;
}
