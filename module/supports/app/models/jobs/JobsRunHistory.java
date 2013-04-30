package models.jobs;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Jobs运行历史.
 */
@Entity
@Table(name="jobs_run_histories")
public class JobsRunHistory extends Model {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobs_define_id", nullable = true)
    public JobsDefine jobsDefine;


    @Column(name="runned_at")
    public Date runnedAt;


}
