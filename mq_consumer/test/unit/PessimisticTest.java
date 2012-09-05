package unit;

import models.PessimisticModel;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.UnitTest;

import javax.persistence.*;

/**
 * @author likang
 *         Date: 12-9-4
 */
public class PessimisticTest extends UnitTest{
    @Test
    public void testPessimistic(){
        EntityManager manager = JPA.newEntityManager();
        manager.getTransaction().begin();
        manager.createNativeQuery("delete from pessimistic_model").executeUpdate();
        manager.createNativeQuery("insert into pessimistic_model(lock_version, value) values(0,10)").executeUpdate();
        manager.getTransaction().commit();
        manager.close();

        EntityManager managerA = JPA.newEntityManager();
        EntityManager managerB = JPA.newEntityManager();

        TypedQuery<PessimisticModel> queryA = managerA.createQuery("select p from PessimisticModel p where p.value = 10", PessimisticModel.class);
        PessimisticModel modelA = queryA.getSingleResult();
        managerA.refresh(modelA, LockModeType.PESSIMISTIC_WRITE);



        TypedQuery<PessimisticModel> queryB = managerB.createQuery("select p from PessimisticModel p where p.value = 10", PessimisticModel.class);
        PessimisticModel modelB = queryB.getSingleResult();
        managerB.getTransaction().begin();
        try{
            managerB.refresh(modelB, LockModeType.PESSIMISTIC_WRITE);
            modelB.value = 12;
            managerB.persist(modelB);
//            managerB.flush();
            fail();
        }catch (PessimisticLockException e){  }
    }
}
