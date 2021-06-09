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
    private int pageNum = 0;
    private boolean hasNext = false;
    private boolean hasPrevious = false;

    public String getSearchText() {
        return searchText == null ? "" : searchText;
    }

    public int getPreviousPageNumber() {
        return pageNum <= 0 ? 0 : pageNum - 1;
    }

    public int getNextPageNumber() {
        return pageNum + 1;
    }


}
