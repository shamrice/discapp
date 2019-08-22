package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "thread")
@SecondaryTable(name = "thread_body", pkJoinColumns = @PrimaryKeyJoinColumn(name = "thread_id", referencedColumnName = "id"))
@Getter
@Setter
@NoArgsConstructor
public class Thread {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    private String submitter;
    private String email;
    private String ipAddress;
    private String subject;
    private boolean showEmail;
    private Boolean deleted;
    private Long parentId;
    private Long discappUserId;
    private Date createDt;
    private Date modDt;

    @Column(table = "thread_body")
    private String body;

}
