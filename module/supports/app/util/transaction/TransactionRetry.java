package util.transaction;

import play.Logger;
import play.db.jpa.JPA;

/**
 * User: tanglq
 * Date: 13-2-17
 * Time: 下午3:58
 */
public class TransactionRetry {
    private static final int MAX_TRIED_TIMES = 10;
    public static <T> T run(TransactionCallback<T> callback) {
        for (int i = 0; i < MAX_TRIED_TIMES; i++) {
            try {
                Logger.info("开始：" + RemoteRecallCheck.getCallId());
                if (!JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().begin();
                    Logger.info("......开始事务" + RemoteRecallCheck.getCallId());
                }
                T result = callback.doInTransaction();
                if (JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().commit();
                    Logger.info("......提交事务" + RemoteRecallCheck.getCallId());
                }
                Logger.info("......返回结果" + RemoteRecallCheck.getCallId() + ", result=" + result);
                return result;
            } catch (RuntimeException e) {
                if (JPA.em().getTransaction().isActive()) {
                    JPA.em().getTransaction().rollback();
                    Logger.info("......回滚事务" + RemoteRecallCheck.getCallId());
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
