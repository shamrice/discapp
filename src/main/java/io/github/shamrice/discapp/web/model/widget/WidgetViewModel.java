package io.github.shamrice.discapp.web.model.widget;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class WidgetViewModel {

    private long applicationId;
    private String styleSheetUrl;
    private String faviconUrl;
    private List<String> threadsHtml;

}
