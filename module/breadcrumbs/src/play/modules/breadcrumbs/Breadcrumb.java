package play.modules.breadcrumbs;

/**
 * 面包屑导航条中的导航链接对象.
 * <p/>
 * User: sujie
 * Date: 3/6/12
 * Time: 4:34 PM
 */
public class Breadcrumb {
    public String url;
    public String desc;

    public Breadcrumb(String desc, String url) {
        this.desc = desc;
        this.url = url;
    }
}
