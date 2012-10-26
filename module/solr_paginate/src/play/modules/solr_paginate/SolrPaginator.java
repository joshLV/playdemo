package play.modules.solr_paginate;

import play.db.jpa.GenericModel;
import play.modules.paginate.Paginator;
import play.modules.solr_paginate.strategy.SolrPageStrategy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 10/26/12
 * Time: 12:29 PM
 */
public class SolrPaginator<T extends GenericModel> extends Paginator<Long, T> implements Serializable {
    private static final long serialVersionUID = -789234905834257567L;

    public SolrPaginator(Class<T> typeToken, List<Long> keys) {
        super(new SolrPageStrategy(typeToken, keys));
    }

    public SolrPaginator(Class<T> typeToken) {
        super(new SolrPageStrategy(typeToken));
    }

    private SolrPaginator(Class<T> typeToken, String filter,
                          Object... params) {
        super(new SolrPageStrategy(typeToken, filter, params));
    }

    public SolrPaginator(Class<T> typeToken, String filter,
                         Map<String, Object> paramMap) {
        super(new SolrPageStrategy(typeToken, filter, paramMap));
    }

    public SolrPaginator(String entityName,
                         String alias, Class<T> typeToken, String filter,
                         Map<String, Object> paramMap) {
        super(new SolrPageStrategy(typeToken, filter, paramMap, entityName,
                alias));
    }
}
