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
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserReadThread {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    private long applicationId;
    private long discappUserId;
    private String readThreads;
    private Date createDt;
    private Date modDt;

}
