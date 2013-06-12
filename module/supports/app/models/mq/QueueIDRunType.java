package models.mq;

/**
 * User: tanglq
 * Date: 13-6-12
 * Time: 下午10:00
 */
public enum QueueIDRunType {
    LAST_IN_FIRST_RUN,    // 最后加入Queue的Message先执行
    ONLY_RUN_FIRST,      // 最早加入Queue的Message会执行，之后的Message在执行前不会加入Queue
}
