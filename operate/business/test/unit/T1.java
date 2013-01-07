package unit;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/6/13
 * Time: 3:09 PM
 */
@Entity
@Table(name = "test1")
public class T1 extends GenericModel {
    @Id
    public Integer num = 1;
    public Integer amount = 0;
    @Column(name = "lock_version")
    @Version
    public int lockVersion=0;
}
