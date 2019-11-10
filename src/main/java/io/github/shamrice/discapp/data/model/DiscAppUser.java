package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "discapp_user")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DiscAppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private Boolean showEmail;

    private Long ownerId;

    private Boolean enabled;

    private Boolean isAdmin;

    private Boolean isUserAccount;

    private Date createDt;
    private Date modDt;
    private Date lastLoginDate;

}
