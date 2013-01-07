package unit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.FunctionalTest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/6/13
 * Time: 2:11 PM
 */
public class MysqlLockTest extends FunctionalTest {
    @Before
    public void setUp() {
        try {
            T1.deleteAll();
            T2.deleteAll();
        } catch (Exception e) {

        }
        T1 t1 = new T1();
        t1.num = 1;
        t1.amount = 0;
        t1.save();
    }

    @Test
    @Ignore
    public void testLock() throws InterruptedException {
/*
        Thread testThread;
        List<Thread> threadList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            final int j = i + 1;

            testThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(")))))))))         Enter MysqlLockTest.run");
                    for (int k = 0; k < 5; k++) {
                        if (k > 0) {
                            System.out.println("k:" + k);
                        }
                        if (insertT2(j)) {
                            return;
                        }
                    }
                }
            });
            testThread.start();
            threadList.add(testThread);
        }
        for (Thread thread : threadList) {
            thread.join();
        }
*/

        ExecutorService executorService =
                new ThreadPoolExecutor(
                        10, // core thread pool size
                        10, // maximum thread pool size
                        1, // time to wait before resizing pool
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(10, true),
                        new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 10; i++) {
            final int j = i + 1;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < 5; k++) {
                        if (k > 0) {
                            System.out.println("k:" + k);
                        }
                        if (insertT2(j)) {
                            return;
                        }
                    }
                }
            });
        }
//        executorService.shutdown();
//
//        if (JPA.local.get() == null) {
//            EntityManager em = JPA.newEntityManager();
//            final JPA jpa = new JPA();
//            jpa.entityManager = em;
//            JPA.local.set(jpa);
//        }
//        List<T2> t2List = T2.find("order by read desc").fetch();
//        System.out.println("t2List.size():" + t2List.size());
//        for (int i = 0; i < 100; i++) {
//            if (t2List.size() < 10) {
//                System.out.println("sleep times:" + (i + 1));
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//
//                }
//            }
//            t2List = T2.find("order by read desc").fetch();
//        }
//        assertEquals(10, t2List.size());
//        for (int i = 1; i < t2List.size(); i++) {
//            System.out.println("t2List.get(" + (i - 1) + ").read:" + t2List.get(i - 1).read);
//            System.out.println("t2List.get(" + i + ").read:" + t2List.get(i).read);
//            assertTrue(t2List.get(i).read < t2List.get(i - 1).read);
//        }
//        T1 t1 = T1.find("num=1").first();
//        assertNotNull(t1);
//        assertEquals(t2List.get(0).read.intValue(), t1.amount.intValue());
//        assertEquals(55, t2List.get(0).result.intValue());
    }

    private boolean insertT2(int j) {
        try {
//            if (JPA.local.get() == null) {
//                EntityManager em = JPA.newEntityManager();
//                final JPA jpa = new JPA();
//                jpa.entityManager = em;
//                JPA.local.set(jpa);
//            }

            T2.append(j);
            System.out.println("append:" + j);
        } catch (Exception e) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e1) {

            }

            return false;
        }
        return true;
    }
}

