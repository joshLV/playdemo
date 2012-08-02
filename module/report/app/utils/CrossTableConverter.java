package utils;

/**
 * 用于提取交叉表数据所使用的数据.
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 *
 * @param <T>
 * @param <V>
 */
public interface CrossTableConverter<T, V> {
    /**
     * 行主键.
     * @param target
     * @return
     */
    public String getRowKey(T target);
    
    /**
     * 列主键.
     * @param target
     * @return
     */
    public String getColumnKey(T target);
    
    /**
     * 把target的值加到oldValue上。
     * @param target
     * @param oldValue
     * @return
     */
    public V addValue(T target, V oldValue);
}
