package util.transaction;

import play.Logger;
import play.db.jpa.JPA;

/**
 * User: tanglq
 * Date: 13-2-17
 * Time: 下午3:58
 */
public class TransactionRetryUtil {
    private static final int MAX_TRIED_TIMES = 15;
    public static <T> T run(TransactionCallback<T> callback) {
        for (int i = 0; i < MAX_TRIED_TIMES; i++) {
            try {
                if (!JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().begin();
                }
                T result = callback.doInTransaction();
                if (JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().commit();
                }

                return result;
            } catch (RuntimeException e) {
                if (JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().rollback();
                }
                e.printStackTrace();
                Logger.info("Found Exception: " + e.getMessage());
                Logger.info("wait 1 seconds." + e.toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    //ignore
                }
            }
        }
        return null;
    }
}
