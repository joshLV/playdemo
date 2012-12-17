package helper;

import play.Play;
import play.mvc.Http;

/**
 * 页面上的域名的工具类.
 * <p/>
 * User: sujie
 * Date: 10/24/12
 * Time: 10:29 AM
 */
public class Domain {

    public static String getWWWHost(Http.Request request) {
        return getHost(request, "www");
    }

    public static String getHomeHost(Http.Request request) {
        return getHost(request, "home");
    }

    private static String getHost(Http.Request request, String subDomain) {
        if (Play.mode.isDev() && request != null && subDomain != null) {
            int port = subDomain.equals("www") ? 9001 : 9002;
            return "http://" + request.domain + ":" + port;
        }
        return "http://" + subDomain + "." + play.Play.configuration.getProperty("application.baseDomain");
    }
}
