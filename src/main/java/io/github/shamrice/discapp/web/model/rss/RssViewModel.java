package io.github.shamrice.discapp.web.model.rss;

import lombok.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RssViewModel {

    @RequiredArgsConstructor
    @Getter
    public static class RssItem {
        private @NonNull String title;
        private @NonNull String link;
        private @NonNull String pubDate;
        private @Setter String description;
    }

    private String feedUrl;
    private long appId;
    private String appName;
    private String appUrl;
    private List<RssItem> rssItems;

}
