package play.modules.paginate;

import java.util.List;
import java.util.ListIterator;

/**
 * 简单的翻页列表对象.
 * <p/>
 * User: sujie
 * Date: 10/26/12
 * Time: 11:23 AM
 */
public class SimplePaginator<T> extends Paginator<Long, T> {
    private static final long serialVersionUID = -7892349233994257567L;

    private List<T> currentPage;

    private Integer rowCount;

    public SimplePaginator(List<T> currentPage) {
        super(null);
        this.currentPage = currentPage;
    }

    @Override
    public int size() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public List<T> getCurrentPage() {
        return currentPage;
    }

    @Override
    public int indexOf(Object o) {
        return currentPage.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return currentPage.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return currentPage.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return currentPage.listIterator(index);
    }

}
