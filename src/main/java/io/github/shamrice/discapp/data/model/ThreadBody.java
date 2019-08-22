package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "thread_body")
@Getter
@Setter
@NoArgsConstructor
public class ThreadBody {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "thread_id")
    private Long threadId;

    private String body;
    private Date createDt;
    private Date modDt;

}
