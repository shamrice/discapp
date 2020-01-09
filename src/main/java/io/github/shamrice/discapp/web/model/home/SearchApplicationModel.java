package io.github.shamrice.discapp.web.model.home;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class SearchApplicationModel {

    @RequiredArgsConstructor
    @Getter
    public static class SearchResult {
        private @NonNull String applicationName;
        private @NonNull String applicationUrl;
    }

    private String infoMessage;

    private Map<Long, SearchResult> searchResults;
    private String baseUrl;
    private String searchText;
}
