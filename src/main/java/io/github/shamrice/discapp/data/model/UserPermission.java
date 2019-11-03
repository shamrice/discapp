package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "user_permission")
@SecondaryTable(name = "discapp_user")
public class UserPermission {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private String userPermissions;
    private Boolean isActive;
    private Date createDt;
    private Date modDt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discapp_user_id")
    private DiscAppUser discAppUser;

}
