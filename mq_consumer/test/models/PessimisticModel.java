package models;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * @author likang
 *         Date: 12-9-4
 */
@Entity
@Table(name = "pessimistic_model")
public class PessimisticModel extends Model {
    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "value")
    public int value;

    public PessimisticModel(){
        this.lockVersion = 0;
        this.value = 10;
    }
}
