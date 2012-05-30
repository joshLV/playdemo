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
        BookEnhance.deleteAll();
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

    @Ignore
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
