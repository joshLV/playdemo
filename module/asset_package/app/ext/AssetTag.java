package ext;

import groovy.lang.Closure;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import play.Play;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;

@FastTags.Namespace("asset")
public class AssetTag extends FastTags {
    
    // 以jvm启动时间作为PROD模式下的VERSION值.
    private static String PROD_VERSION = String.valueOf(System.currentTimeMillis());
    
    /**
     * 用法：
     * 
     * <pre>
     *   #{asset.css src:["a.css", "b.css"], package:true /}
     * </pre>
     * 
     * Package在Dev Mode下默认为false，在Prod Mode下默认为true
     * 
     * @param args
     * @param body
     * @param out
     * @param template
     * @param fromLine
     */
    public static void _css(Map<?, ?> args, Closure body, PrintWriter out,
            ExecutableTemplate template, int fromLine) {
        
        String tagVersion = Play.mode.isDev() ? String.valueOf(System.currentTimeMillis()) : PROD_VERSION;

        List<String> srcList = getSrcArg(args);
        boolean bPackageFlag = getPackageFlagArg(args);
        
        String baseUrl = getBaseUrl() + "/css";

        if (bPackageFlag) {
            start_css_tag(out);
            StringBuilder sb = new StringBuilder();
            for (String src : srcList) {
                if (!src.startsWith("/")) sb.append("/");
                sb.append(src);
            }
            out.print("href=\"" + baseUrl + sb.toString() + "/" + tagVersion + ".css\"");
            end_css_tag(out);
        } else {
            for (String src : srcList) {
                start_css_tag(out);
                if (!src.startsWith("/")) src = "/" + src;
                out.print("href=\"" + baseUrl + src + "?" + tagVersion + "\"");
                end_css_tag(out);
            }
        }
    }
    
    /**
     * 用法：
     * 
     * <pre>
     *   #{asset.js src:["a.js", "b.js"], package:true /}
     * </pre>
     * @param args
     * @param body
     * @param out
     * @param template
     * @param fromLine
     */
    public static void _js(Map<?, ?> args, Closure body, PrintWriter out,
            ExecutableTemplate template, int fromLine) {
        
        String tagVersion = Play.mode.isDev() ? String.valueOf(System.currentTimeMillis()) : PROD_VERSION;

        List<String> srcList = getSrcArg(args);
        boolean bPackageFlag = getPackageFlagArg(args);
        
        String baseUrl = getBaseUrl() + "/js";

        if (bPackageFlag) {
            start_js_tag(out);
            StringBuilder sb = new StringBuilder();
            for (String src : srcList) {
                if (!src.startsWith("/")) sb.append("/");
                sb.append(src);
            }
            out.print("src=\"" + baseUrl + sb.toString() + "/" + tagVersion + ".js\"");
            end_js_tag(out);
        } else {
            for (String src : srcList) {
                start_js_tag(out);
                if (!src.startsWith("/")) src = "/" + src;
                out.print("src=\"" + baseUrl + src + "?" + tagVersion + "\"");
                end_js_tag(out);
            }
        }
    }    

    /**
     * 用法：
     * 
     * <pre>
     *   #{asset.url href:"/test.png" /}
     * </pre>
     * @param args
     * @param body
     * @param out
     * @param template
     * @param fromLine
     */
    public static void _url(Map<?, ?> args, Closure body, PrintWriter out,
            ExecutableTemplate template, int fromLine) {
        
        String tagVersion = Play.mode.isDev() ? String.valueOf(System.currentTimeMillis()) : PROD_VERSION;

        String href = args.get("href").toString();
        String baseUrl = getBaseUrl();
        String split = href.indexOf('?') > 0 ? "&" : "?";

        out.print(baseUrl + href + split + tagVersion + "=true");
    }    
    
    private static String getBaseUrl() {
        String cdnHost = Play.configuration.getProperty("cdn.host");
        if (cdnHost == null) {
            cdnHost = "a.uhcdn.com";
        }
        return "http://" + cdnHost;
    }

    private static boolean getPackageFlagArg(Map<?, ?> args) {
        boolean bPackageFlag = false;
        Object oPackageFlag = args.get("package");
        if (oPackageFlag == null) {
            if (Play.mode.isProd()) {
                bPackageFlag = true;
            }
        } else {
            bPackageFlag = Boolean.parseBoolean(oPackageFlag.toString());
        }
        return bPackageFlag;
    }

    private static List<String> getSrcArg(Map<?, ?> args) {
        Object oSrc = args.get("src");
        List<String> srcList = null;
        if (oSrc instanceof List) {
            srcList = (List) oSrc;
        } else {
            srcList = new ArrayList<>();
            srcList.add(oSrc.toString());
        }
        return srcList;
    }

    private static void end_css_tag(PrintWriter out) {
        out.print("/>");
    }

    private static void start_css_tag(PrintWriter out) {
        out.print("<link rel=\"stylesheet\" type=\"text/css\" "); // href="");
    }
    
    // <script language="javaScript" src="http://www.51cto.com/php/base_art.js"></SCRIPT>
    
    private static void end_js_tag(PrintWriter out) {
        out.print("></script>");
    }

    private static void start_js_tag(PrintWriter out) {
        out.print("<script language=\"javaScript\" ");
    }
}
