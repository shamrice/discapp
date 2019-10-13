package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ApplicationPermission {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private Boolean displayIpAddress;
    private Boolean blockBadWords;
    private Boolean blockSearchEngines;
    private String allowHtmlPermissions;
    private String unregisteredUserPermissions;
    private String registeredUserPermissions;
    private Date createDt;
    private Date modDt;
}
