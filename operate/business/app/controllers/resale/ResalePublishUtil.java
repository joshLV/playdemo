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
    static Pattern imgTagPattern = Pattern.compile("<img[^>]*src=[\"']([^\"^']*)", Pattern.CASE_INSENSITIVE);
    static Pattern uhcdnPPattern =  Pattern.compile("(.+)uhcdn\\.com/p/(\\d+)/(\\d+)/(\\d+)/[a-z0-9]{8}_(.+)");
    static Pattern uhcdnOPattern = Pattern.compile("(.+)uhcdn\\.com/o/(\\d+)/(\\d+)/(\\d+)/(.+)");

    public static String replaceImgUrlWithJdLogo(String param) {
        if (StringUtils.isBlank(param)){
            return param;
        }
        Matcher m = imgTagPattern.matcher(param);
        while (m.find()){
            String src = m.group(1);
            if (src.contains("_jd")){
                continue;
            }
            Matcher matcher = uhcdnPPattern.matcher(src);
            if (!matcher.matches()) {
                matcher = uhcdnOPattern.matcher(src);
                if (!matcher.matches()) {
                    continue;
                }
            }
            String newSrc = matcher.group(1)
                    + "uhcdn.com/p/"
                    + matcher.group(2) + "/"
                    + matcher.group(3) + "/"
                    + matcher.group(4) + "/"
                    + PathUtil.signImgPath( PathUtil.addImgPathMark(matcher.group(5), "jd") );
            param = param.replaceAll(src, newSrc);
        }
        return param;
    }
}
