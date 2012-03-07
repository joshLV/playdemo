package play.modules.breadcrumbs;

import java.util.ArrayList;

/**
 * 面包屑导航列表对象，方便生成较长的导航条.
 * <p/>
 * User: sujie
 * Date: 3/7/12
 * Time: 10:41 AM
 */
public class BreadcrumbList extends ArrayList<Breadcrumb> {

    public BreadcrumbList(String... crumbArgs) {
        for (int i = 0; i < crumbArgs.length; i += 2) {
            String desc = crumbArgs[i];
            String url = crumbArgs[i + 1];
            add(new Breadcrumb(desc, url));
        }
    }

    public BreadcrumbList append(String desc, String url) {
        add(new Breadcrumb(desc, url));
        return this;
    }
}
