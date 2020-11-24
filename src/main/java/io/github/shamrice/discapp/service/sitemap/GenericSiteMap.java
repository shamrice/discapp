package io.github.shamrice.discapp.service.sitemap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor
public class GenericSiteMap {

    @Getter
    @Setter
    private List<GenericSiteMapItem> genericSiteMapItems = new ArrayList<>();
}
