package models.sales;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * @author likang
 *         Date: 12-3-13
 */
public class ContentSafeFilter {
    public final static Whitelist user_content_filter = Whitelist.relaxed();

    static {
        //增加可信标签到白名单
        user_content_filter.addTags("embed", "object", "param", "span", "div");
        //增加可信属性
        user_content_filter.addAttributes(":all", "style", "class", "id", "name");
        user_content_filter.addAttributes("object", "width", "height", "classid", "codebase");
        user_content_filter.addAttributes("param", "name", "value");
        user_content_filter.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen", "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }

    /**
     * 对用户输入内容进行过滤
     *
     * @param html
     * @return
     */
    public static String filter(String html) {
        if (StringUtils.isBlank(html)) return "";
        return Jsoup.clean(html, user_content_filter);
        //return filterScriptAndStyle(html);
    }

    /**
     * 比较宽松的过滤，但是会过滤掉object，script， span,div等标签，适用于富文本编辑器内容或其他html内容
     *
     * @param html
     * @return
     */
    public static String relaxed(String html) {
        return Jsoup.clean(html, Whitelist.relaxed());
    }

    /**
     * 去掉所有标签，返回纯文字.适用于textarea，input
     *
     * @param html
     * @return
     */
    public static String pureText(String html) {
        return Jsoup.clean(html, Whitelist.none());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String unsafe = "<table><tr><td>1</td></tr></table>" +
                "<img src='' alt='' />" +
                "<p><a href='http://example.com/' onclick='stealCookies()'>Link</a>" +
                "<object></object>" +
                "<script>alert(1);</script>" +
                "</p>";
        String safe = ContentSafeFilter.filter(unsafe);
    }
}