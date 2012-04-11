/**
 *  This file is part of LogiSima-play-cas.
 *
 *  LogiSima-play-cas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  LogiSima-play-cas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with LogiSima-play-cas.  If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.modules.resale.cas;

import models.resale.Resaler;
import play.Logger;
import play.cache.Cache;
import play.modules.resale.cas.CASUtils;
import play.modules.resale.cas.annotation.Check;
import play.modules.resale.cas.models.CASUser;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import controllers.modules.resale.cas.annotations.SkipCAS;

/**
 * This class is a part of the play module secure-cas. It add the ability to check if the user have access to the
 * request. If the user is note logged, it redirect the user to the CAS login page and authenticate it.
 *
 * @author bsimard
 *
 */
public class SecureCAS extends Controller {

    public static final String SESSION_USER_KEY = "resale_login";

    @Before
    public static void setResaler() {
    	Resaler resaler = getResaler();
    	
    	System.out.println("resaler>>>>>>>>>>>>>>>>>." + resaler);
        Cas cas = new Cas();
        if (resaler != null) {
            cas.isLogin = true;
            cas.username = resaler.loginName;
            cas.resaler = resaler;
        }
        renderArgs.put("cas", cas);
    }

    public static Resaler getResaler() {
        String loginName = session.get(SESSION_USER_KEY);
        
        System.out.println("loginName=" + loginName);
        
        if (loginName == null || "".equals(loginName)) {
            return null;
        }
        return Resaler.find("byLoginName", loginName).first();
    }    

    /**
     * Action for the login route. We simply redirect to CAS login page.
     *
     * @throws Throwable
     */
    public static void login() throws Throwable {
        // We must avoid infinite loops after success authentication
        if (!Router.route(request).action.equals("modules.resale.cas.SecureCAS.login")) {
            // we put into cache the url we come from
            Cache.add("url_" + session.getId(), request.method == "GET" ? request.url : "/", "10min");
        }

        // we redirect the user to the cas login page
        String casLoginUrl = CASUtils.getCasLoginUrl(false);
        redirect(casLoginUrl);
    }

    /**
     * Action for the logout route. We clear cache & session and redirect the user to CAS logout page.
     *
     * @throws Throwable
     */
    public static void logout() throws Throwable {

        String username = session.get(SESSION_USER_KEY);

        // we clear cache
        Cache.delete("pgt_" + username);

        // we clear session
        session.clear();

        // we invoke the implementation of "onDisconnected"
        Security.invoke("onDisconnected");

        // we redirect to the cas logout page.
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        redirect(casLogoutUrl);
    }

    /**
     * Action when the user authentification or checking rights fails.
     *
     * @throws Throwable
     */
    public static void fail() throws Throwable {
        forbidden();
    }

    /**
     * Action for the CAS return.
     *
     * @throws Throwable
     */
    public static void authenticate() throws Throwable {
        Boolean isAuthenticated = Boolean.FALSE;
        String ticket = params.get("ticket");
        if (ticket != null) {
            Logger.debug("[SecureCAS]: Try to validate ticket " + ticket);
            CASUser user = CASUtils.valideCasTicket(ticket);
            if (user != null) {
                isAuthenticated = Boolean.TRUE;
                session.put(SESSION_USER_KEY, user.getUsername());
                // we invoke the implementation of onAuthenticate
                Security.invoke("onAuthenticated", user);
            }
        }

        if (isAuthenticated) {
            // we redirect to the original URL
            String url = (String) Cache.get("url_" + session.getId());
            Cache.delete("url_" + session.getId());
            if (url == null) {
                url = "/";
            }
            Logger.debug("[SecureCAS]: redirect to url -> " + url);
            redirect(url);
        }
        else {
            fail();
        }
    }

    /**
     * Action for the proxy call back url.
     */
    public static void pgtCallBack() throws Throwable {
        // CAS server call this URL with PGTIou & PGTId
        String pgtIou = params.get("pgtIou");
        String pgtId = params.get("pgtId");

        // here we put in cache PGT with PGTIOU as key
        if (pgtIou != null || pgtId != null) {
            Cache.set(pgtIou, pgtId);
        }
    }


    private static boolean skipCAS() {
        if (getActionAnnotation(SkipCAS.class) != null ||
                getControllerInheritedAnnotation(SkipCAS.class) != null) {
            return true;
        }
        return false;
    }

    /**
     * Method that do CAS Filter and check rights.
     *
     * @throws Throwable
     */
    @Before(unless = { "login", "logout", "fail", "authenticate", "pgtCallBack" })
    public static void filter() throws Throwable {
        Logger.debug("[SecureCAS]: CAS Filter for URL -> " + request.url);

        if (skipCAS()) {
            Logger.debug("[SecureCAS]: Skip CAS.");
            return;
        }

        // if user is authenticated, the username is in session !
        if (!session.contains(SESSION_USER_KEY)) {
            Logger.debug("[SecureCAS]: user is not authenticated");
            // we put into cache the url we come from
            Cache.add("url_" + session.getId(), request.method == "GET" ? request.url : "/", "10min");

            // we redirect the user to the cas login page
            String casLoginUrl = CASUtils.getCasLoginUrl(true);
            redirect(casLoginUrl);
        }
    }

    /**
     * Function to check the rights of the user. See your implementation of the Security class with the method check.
     *
     * @param check
     * @throws Throwable
     */
    private static void check(Check check) throws Throwable {
        for (String profile : check.value()) {
            boolean hasProfile = (Boolean) Security.invoke("check", profile);
            if (!hasProfile) {
                Security.invoke("onCheckFailed", profile);
            }
        }
    }

}
