package play.modules.paginate;

import play.db.jpa.GenericModel;
import play.modules.paginate.strategy.RecordLocatorStrategy;

import java.io.Serializable;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 10/26/12
 * Time: 11:23 AM
 */
public class SimplePaginator<T extends GenericModel> extends Paginator<Long, T> implements Serializable {
    private static final long serialVersionUID = -789234923454257567L;
    private static final String DEFAULT_PAGE_PARAM = "page";

    private int pageSize;
    private List<T> currentPage;

    private int pageNumber;
    private Integer rowCount;
    private String paramName;

    private static final int DEFAULT_PAGE_SIZE = 20;

    protected SimplePaginator(RecordLocatorStrategy recordLocatorStrategy) {
        super(recordLocatorStrategy);
    }


}
