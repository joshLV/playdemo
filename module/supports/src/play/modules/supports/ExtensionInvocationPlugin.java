package play.modules.supports;

import play.Logger;
import play.PlayPlugin;
import play.mvc.Router;
import util.extension.ExtensionInvoker;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午5:57
 */
public class ExtensionInvocationPlugin extends PlayPlugin {
    @Override
    public void onApplicationStart() {
        ExtensionInvoker.initExtensions();
        Logger.info("Module yabo.Support: " + ExtensionInvocationPlugin.class.getName() + " loaded.");
        for (Class<?> key : ExtensionInvoker.extensionMap.keySet()) {
            Logger.info("    " + key.getName() + ": Found " + ExtensionInvoker.extensionMap.get(key).size() + " " +
                    "Invocation.");
        }
    }

    @Override
    public void onRoutesLoaded() {
        Logger.debug("adding routes for yabo.Support:" + ExtensionInvocationPlugin.class.getName());
        Router.addRoute("GET", "/@extensions", "operate.cas.MockServer.login");
    }
}
