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
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String key;
    private Integer code;
    private Boolean isRedeemed;
    private Date createDt;
    private Date expDt;
}
