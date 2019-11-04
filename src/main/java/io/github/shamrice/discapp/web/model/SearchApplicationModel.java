package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class SearchApplicationModel {

    Map<String, String> searchResults;
    String baseUrl;
    String searchText;
}
