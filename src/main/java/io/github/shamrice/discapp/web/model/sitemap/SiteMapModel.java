package io.github.shamrice.discapp.web.model.sitemap;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SiteMapModel {

    @RequiredArgsConstructor
    @Getter
    public static class SiteMapItem {
        private @NonNull String url;
        private @NonNull String lastModified;
    }

    private List<SiteMapModel.SiteMapItem> siteMapItems;
}
