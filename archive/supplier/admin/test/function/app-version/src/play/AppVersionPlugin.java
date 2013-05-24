package play;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import play.libs.IO;
import play.vfs.VirtualFile;
import play.mvc.Router;


public class AppVersionPlugin extends PlayPlugin {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void afterApplicationStart() {
        AppVersion.name = Play.configuration.getProperty("application.name");
        AppVersion.startupAt = new Date();

        VirtualFile file = VirtualFile.fromRelativePath("conf/version.conf");

        if (file.exists()) {
            Logger.info("conf/version.conf exists.");
            Properties p = IO.readUtf8Properties(file.inputstream());

            AppVersion.buildAt = parseDate(p.getProperty("build.time"));
            AppVersion.value = p.getProperty("version", "empty");
            AppVersion.revision = p.getProperty("revision", "empty");
        } else {
            Logger.info("conf/version.conf FOUNDED.");
            AppVersion.buildAt = new Date();
            AppVersion.value = "trunk";
            AppVersion.revision = "-1";
        }
    }

    private Date parseDate(String string) {
        Date date = null;
        try {
            if (!StringUtils.isEmpty(string)) {
                date = simpleDateFormat.parse(string);
            } else {
                date = simpleDateFormat.parse("2012-01-01 12:00:00");
            }
        } catch (ParseException e) {
            Logger.warn(e, e.getMessage());
            date = new Date();  //尽量不返回null
        }
        return date;
    }

    @Override
    public void onRoutesLoaded() {
        Logger.debug("adding routes /@appversion for AppVersion");
        Router.addRoute("GET", "/@appversion", "appversion.Versions.index");
    }

}
