package controllers.appversion;

import play.AppVersion;
import play.mvc.Controller;

import java.text.SimpleDateFormat;

public class Versions extends Controller {

    public static void index() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append(AppVersion.name).append(" ").append(AppVersion.value)
            .append("\n").append("revision:").append(AppVersion.revision)
            .append("\n").append("BuildAt:").append(sdf.format(AppVersion.buildAt))
            .append("\n").append("StartUp:").append(sdf.format(AppVersion.startupAt));
        renderText(sb.toString());
    }
}
