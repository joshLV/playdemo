package unit;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import models.Book;
import models.BookEnhance;
import models.MyBook;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.UnitTest;

public class OptimisticLockingTest extends UnitTest{
    
    @Before
    public void setup(){
        Book.deleteAll();
        MyBook.deleteAll();
        Book book = new Book();
        book.count = 0L;
        book.save();
        
        MyBook mybook = new MyBook();
        mybook.count = 0L;
        mybook.save();
        
        BookEnhance book2 = new BookEnhance();
        book2.count = 0L;
        book2.save();
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
    @Ignore
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
    public void testWithoutOptimisticLocking() throws Exception{
        
        Thread a = new Thread(){
            
            public void run(){  
                JPAPlugin.startTx(false);
                System.out.println("Thread1");
                Book book = (Book)Book.findAll().get(0);
                try {
                    sleep(300l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread2");
                book.count = 2L;
                book.save();
                System.out.println("Thread3");
            }
        };        
        
        Book book = (Book)Book.findAll().get(0);
        System.out.println("Main0");
        a.start();
        Thread.sleep(100l);
        System.out.println("Main1");
        book.count = 3L;
        book.save();
        System.out.println("Main2");
        Thread.sleep(300l);
        System.out.println("Main2.5");
        JPAPlugin.closeTx(false);
        JPAPlugin.startTx(false);
        book = (Book)Book.findAll().get(0);
        System.out.println("Main3");
        assertEquals(new Long(2l), book.count);
    }
    
    @Test
    public void testWitOptimisticLocking() throws Exception{
        
        Thread a = new Thread(){
            
            public void run(){  
                JPAPlugin.startTx(false);
                System.out.println("Thread1");
                MyBook book = (MyBook)MyBook.findAll().get(0);
                try {
                    sleep(300l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread2");
                book.count = 2L;
                book.save();
                System.out.println("Thread3");
            }
        };        
        
        MyBook book = (MyBook)MyBook.findAll().get(0);
        System.out.println("Main0");
        a.start();
        Thread.sleep(100l);
        System.out.println("Main1");
        book.count = 3L;
        book.save();
        System.out.println("Main2");
        Thread.sleep(300l);
        System.out.println("Main2.5");
        JPAPlugin.closeTx(false);
        JPAPlugin.startTx(false);
        book = (MyBook)MyBook.findAll().get(0);
        System.out.println("Main3");
        assertEquals(new Long(3l), book.count);
    }
    
    
    @Test
    public void testWitOptimisticLockingAndRetry() throws Exception{
        
        Thread a = new Thread(){
            
            public void updateCount(long id, long count) {
                MyBook book = MyBook.findById(id);
                book.count = count;
                book.save();
            }
            
            public void run(){  
                JPAPlugin.startTx(false);
                System.out.println("Thread1");
                MyBook book = (MyBook)MyBook.findAll().get(0);
                long id = book.id;
                System.out.println("@book.count=" + book.count + ",version=" + book.version);
                try {
                    sleep(300l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread2");
                book.count = 2L;
                try {
                    updateCount(id, 2L);
                } catch(OptimisticLockException e) {
                    JPAPlugin.closeTx(true);
                    JPAPlugin.startTx(false);
                    updateCount(id, 2L);
                }
                System.out.println("Thread3");
            }
        };        
        
        MyBook book = (MyBook)MyBook.findAll().get(0);
        System.out.println("Main0");
        a.start();
        Thread.sleep(100l);
        System.out.println("Main1");
        book.count = 3L;
        book.save();
        JPAPlugin.closeTx(false);
        System.out.println("Main2");
        Thread.sleep(300l);
        System.out.println("Main2.5");

        JPAPlugin.startTx(false);
        book = (MyBook)MyBook.findAll().get(0);
        System.out.println("Main3");
        assertEquals(new Long(2l), book.count);
    }
    
    
    @Test
    public void testWithLockAndRetry() throws Exception{
        
        Thread a = new Thread(){
            
            public void run(){  
                JPAPlugin.startTx(false);
                System.out.println("Thread1");
                BookEnhance book = (BookEnhance)BookEnhance.findAll().get(0);
                System.out.println("@book.count=" + book.count + ",version=" + book.version);
                try {
                    sleep(300l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread2");
                book.count = 2L;
                try {
                    book.save();
                } catch(OptimisticLockException e) {

                    JPAPlugin.closeTx(true);
                    JPAPlugin.startTx(false);
                    System.out.println("book.count=" + book.count + ",version=" + book.version);
                    book = (BookEnhance)BookEnhance.findAll().get(0);
                    System.out.println("book2.count=" + book.count + ",version=" + book.version);
                    book.count = 2L;
                    book.save();
                }
                System.out.println("Thread3");
            }
        };        
        
        BookEnhance book = (BookEnhance)BookEnhance.findAll().get(0);
        System.out.println("Main0");
        a.start();
        Thread.sleep(100l);
        System.out.println("Main1");
        book.count = 3L;
        book.save();
        book.em().flush();
        JPAPlugin.closeTx(false);
        System.out.println("Main2");
        Thread.sleep(300l);
        System.out.println("Main2.5");

        JPAPlugin.startTx(false);
        book = (BookEnhance)BookEnhance.findAll().get(0);
        System.out.println("Main3");
        assertEquals(new Long(2l), book.count);
    }
    
}
