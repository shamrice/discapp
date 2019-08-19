package io.github.shamrice.discapp.data.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Stats {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private String statDate;
    private Long uniqueIps;
    private Long pageViews;
    private Date createDt;
    private Date modDt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public Long getUniqueIps() {
        return uniqueIps;
    }

    public void setUniqueIps(Long uniqueIps) {
        this.uniqueIps = uniqueIps;
    }

    public Long getPageViews() {
        return pageViews;
    }

    public void setPageViews(Long pageViews) {
        this.pageViews = pageViews;
    }

    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    public Date getModDt() {
        return modDt;
    }

    public void setModDt(Date modDt) {
        this.modDt = modDt;
    }
}
