package io.github.shamrice.discapp.service.sitemap;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class GenericSiteMapItem {

    @Getter
    private @NonNull String forumUrl;

    @Getter
    private @NonNull String articleUrl;

    @Getter
    private @NonNull String lastModified;
}
