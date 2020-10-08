package io.github.shamrice.discapp.web.model.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AdminReportFrequencyModel {
    private String changeReportFrequency;
    private String updateReportFrequency;
    private String emailAddress;
    private String authCode;
    private Long appId;
    private String appName;

    private String infoMessage;
    private String errorMessage;
    private String baseUrl;

}
