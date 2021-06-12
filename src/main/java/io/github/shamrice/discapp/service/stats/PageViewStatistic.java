package io.github.shamrice.discapp.service.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
class PageViewStatistic {

    private long applicationId;
    private String ipAddress;
}
