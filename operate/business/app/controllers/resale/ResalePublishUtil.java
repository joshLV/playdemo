package controllers.resale;

import com.uhuila.common.util.PathUtil;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 13-4-23
 */
public class ResalePublishUtil {
    static Pattern jdLogoPattern = Pattern.compile("<img[^>]*src=[\"']([^\"^']*)", Pattern.CASE_INSENSITIVE);
    static Pattern urlWithoutSignPattern =  Pattern.compile("(.+/)[a-z0-9]{8}_(.+)");

    public static String replaceImgUrlWithJdLogo(String param) {
        if (StringUtils.isBlank(param)){
            return param;
        }
        Matcher m = jdLogoPattern.matcher(param);
        while (m.find()){
            String src = m.group(1);
            System.out.println("======\n" + src);
            if (src.contains("_jd")){
                continue;
            }
            Matcher urlWithoutSignMatcher = urlWithoutSignPattern.matcher(src);
            if (urlWithoutSignMatcher.matches()) {
                String newSrc = urlWithoutSignMatcher.group(1) + PathUtil.signImgPath(
                        PathUtil.addImgPathMark(urlWithoutSignMatcher.group(2), "jd")
                );
                param = param.replaceAll(src, newSrc);
            }
        }
        return param;
    }
}
