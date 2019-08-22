package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reported_abuse")
@Getter
@Setter
@NoArgsConstructor
public class ReportedAbuse {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    private Long threadId;
    private String ipAddress;
    private Long reportedBy;
    private Date createDt;
    private Date modDt;
}
