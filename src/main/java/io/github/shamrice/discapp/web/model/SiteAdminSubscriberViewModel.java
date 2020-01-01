package io.github.shamrice.discapp.web.model;

import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SiteAdminSubscriberViewModel {
    private String infoMessage;
    private String errorMessage;
    private List<ApplicationSubscription> applicationSubscriptions;
}
