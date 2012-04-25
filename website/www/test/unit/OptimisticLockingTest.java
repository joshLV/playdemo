package unit;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import models.Book;
import models.BookEnhance;
import models.MyBook;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.PlayPlugin;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Transactional;
import play.test.FunctionalTest;
import play.test.UnitTest;

@Ignore
public class OptimisticLockingTest extends UnitTest{
    
//    @Before
    public void setup(){
        Book.deleteAll();
        MyBook.deleteAll();
        Book book = new Book();
        book.count = 0L;
        book.save();
        
        MyBook mybook = new MyBook();
        mybook.count = 0L;
        mybook.save();
    }
/*    
    private void justrun(final String url){
        List<Thread> threads = new ArrayList<>(50);
        for(int i = 0;i < 300; i++){
            final int j = i;
            threads.add( new Thread(){
                public void run(){
                    GET(url + "?i="+j);
                }}
            );
        }
        for(Thread t : threads){
            t.start();
        }
    }
*/    
    @Test
    public void testWithOptimisticLocking(){

        EntityManager session = JPA.newEntityManager();
        session.getTransaction().begin();
        BookEnhance book = ((List<BookEnhance>)(session.createQuery("select b from BookEnhance b").getResultList())).get(0);
        BookEnhance book2 = (BookEnhance)session.createQuery("").getSingleResult();
        book.count += 1;
//        session.persist(book);
        session.flush();
        book.save();
        /*
        BookEnhance book = new BookEnhance();
        book.count = 10L;
        book.id = null;
        session.persist(book);
        */
        session.getTransaction().commit();
        session.close();
        /*        
        BookEnhance bookA = (BookEnhance)BookEnhance.findById(1L);
        BookEnhance bookB = (BookEnhance)BookEnhance.findById(1L);
        bookA.count += 1;
        bookA.save();
        bookB.count += 1;
        bookB.save();
*/
//        BookEnhance bookB = (BookEnhance)
//        justrun("/book");
/*        JPAPlugin.startTx(false);
     EntityManager sessionA =     JPA.newEntityManager();
     sessionA.setFlushMode(FlushModeType.COMMIT);
     sessionA.getTransaction().begin();
     EntityManager sessionB = JPA.newEntityManager();
     sessionB.setFlushMode(FlushModeType.COMMIT);
     sessionB.getTransaction().begin();
     BookEnhance bookA =  (BookEnhance)sessionA.createQuery("select b from BookEnhance b where b.id = 1").getSingleResult();
     BookEnhance bookB =  (BookEnhance)sessionB.createQuery("select b from BookEnhance b where b.id = 1").getSingleResult();
     System.out.println(sessionA.contains(bookA));
     bookA.count += 1;
     System.out.println("11:" + bookA.version);
//     sessionA.flush();
     sessionA.getTransaction().commit();
     sessionA.close();
     bookB.count += 1;
     System.out.println("22:" + bookB.version);
     sessionB.refresh(bookB);
     bookB.count += 1;
     System.out.println("33:" + bookB.version);
     //sessionB.flush();
     sessionB.getTransaction().commit();
     sessionB.close();
     JPAPlugin.closeTx(false);
*/     
     
//        justrun("/mybook");
//        justrun("/bookenhance");
    }
    
    
    @Test
    @Ignore
    public void testWithoutOptimisticLocking(){
        JPAPlugin.startTx(false);
        Thread a = new Thread(){
            Book book = (Book)Book.findAll().get(0);
            public void run(){
                book.count = 2L;
                book.save();
//                JPAPlugin.closeTx(false);
                System.out.println("a");
            }
        };        
        a.start();
    }
    
}
