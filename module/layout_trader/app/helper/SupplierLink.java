package helper;

import play.Play;
import play.supplier.cas.CASUtils;

/**
 * User: yan
 * Date: 13-4-11
 * Time: 下午5:05
 */
public class SupplierLink {

    public static String CURRENT_APP_NAME = Play.configuration.getProperty("application.name");
    public static String BASE_DOMAIN = Play.configuration.getProperty("application.baseDomain");

    public static String getKtvLink(String path) {
        if ("supplier_ktv".equals(CURRENT_APP_NAME)) {
            return path;
        }
        return "http://" + CASUtils.getSubDomain() + ".ktv." + BASE_DOMAIN + path;
    }

    public static String getHomeLink(String path) {
        if ("traders-home".equals(CURRENT_APP_NAME)) {
            return path;
        }
        return String.format("%s%s%s%s%s", "http://", CASUtils.getSubDomain(), ".", BASE_DOMAIN, path);
    }
}
