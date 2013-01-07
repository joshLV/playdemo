package unit;

import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/6/13
 * Time: 3:10 PM
 */
@Entity
@Table(name = "test20")
public class T2 extends Model {
    public Integer read;
    public Integer result;

    public static void append(int i) {
        System.out.println(")))))))))         Enter T2.append");

        JPA.em().getTransaction().begin();
        System.out.println("JPA.em():" + JPA.em());
        Query q = JPA.em().createNativeQuery("select amount from test1 where num=1 for update");
        Integer col1 = (Integer) q.getSingleResult();
        System.out.println("col1:" + col1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //ignore
        }

        T2 t2 = new T2();
        t2.read = col1;
        System.out.println("t2.read:" + t2.read + ",i:" + i);
        t2.result = t2.read + i;
        t2.save();

        q = JPA.em().createQuery("select t from T1 t where t.num=1");

        T1 t1 = (T1) q.getSingleResult();
        t1.amount = t1.amount + i;
        t1.save();
        JPA.em().getTransaction().commit();
    }
}
