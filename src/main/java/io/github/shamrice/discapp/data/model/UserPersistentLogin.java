package io.github.shamrice.discapp.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "persistent_logins")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserPersistentLogin {

    private String username;

    @Id
    private String series;

    private String token;
    private Date lastUsed;
}
