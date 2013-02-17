package util.transaction;

/**
 * User: tanglq
 * Date: 13-2-17
 * Time: 下午3:59
 */
public interface TransactionCallback<T> {
    /**
     * 事务回调，这个方法的操作是在事务中，如果返回false或抛出异常，都会重试；如果返回true，则成功提交.
     * @return
     */
    public T doInTransaction();
}
