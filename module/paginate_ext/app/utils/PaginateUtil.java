package utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import play.modules.paginate.MappedPaginator;
import play.modules.paginate.ValuePaginator;

public class PaginateUtil {
    
    /**
     * 把List结果包装为分页对象.
     * @param resultList
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static <T> ValuePaginator<T> wrapValuePaginator(List<T> resultList, int pageNumber,
            int pageSize) {
        ValuePaginator<T> mappedPage = new ValuePaginator<>(resultList);
        mappedPage.setPageNumber(pageNumber);
        mappedPage.setPageSize(pageSize);        
        return mappedPage;
    }    
}
