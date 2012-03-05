package play.modules.paginate;

import play.db.jpa.GenericModel;
import play.modules.paginate.strategy.JPAExtStrategy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * JPA的play的翻页.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 9:50 AM
 */
public class JPAExtPaginator<T extends GenericModel> extends Paginator<Long, T> implements Serializable {
    private static final long serialVersionUID = -789234905834257567L;

    public JPAExtPaginator(Class<T> typeToken, List<Long> keys) {
        super(new JPAExtStrategy(typeToken, keys));
    }

    public JPAExtPaginator(Class<T> typeToken) {
        super(new JPAExtStrategy(typeToken));
    }

    private JPAExtPaginator(Class<T> typeToken, String filter,
                           Object... params) {
        super(new JPAExtStrategy(typeToken, filter, params));
    }

    public JPAExtPaginator(Class<T> typeToken, String filter,
                           Map<String, Object> paramMap) {
        super(new JPAExtStrategy(typeToken, filter, paramMap));
    }

    public JPAExtPaginator(String entityName,
                           String alias, Class<T> typeToken, String filter,
                           Map<String, Object> paramMap) {
        super(new JPAExtStrategy(typeToken, filter, paramMap, entityName,
                alias));
    }

    public JPAExtPaginator<T> orderBy(String orderByClause) {
        jpaStrategy().setOrderBy(orderByClause);
        return this;
    }

    public JPAExtStrategy jpaStrategy() {
        return (JPAExtStrategy) getRecordLocatorStrategy();
    }
}