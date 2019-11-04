package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reported_abuse")
@Getter
@Setter
@ToString
@NoArgsConstructor
@SecondaryTable(name = "thread")
public class ReportedAbuse {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    private String ipAddress;
    private Long reportedBy;
    private Boolean isDeleted;
    private Date createDt;
    private Date modDt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private Thread thread;
}
