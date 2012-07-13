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
package controllers.modules.website.cas;

import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.consumer.UserLoginHistory;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.modules.website.cas.CASUtils;
import play.modules.website.cas.annotation.Check;
import play.modules.website.cas.models.CASUser;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Router;

import java.util.Date;

/**
 * This class is a part of the play module secure-cas. It add the ability to check if the user have access to the
 * request. If the user is note logged, it redirect the user to the CAS login page and authenticate it.
 *
 * @author bsimard
 */
public class SecureCAS extends Controller {

    public static final String SESSION_USER_KEY = "website_login";

    @Before
    public static void addTrace() {
        Logger.debug("[SecureCAS]: check cookie identity");
        Http.Cookie cookieId = request.cookies.get("identity");
        if (cookieId == null) {
            Logger.debug("[SecureCAS]: set a new cookie identity");
            String baseDomain = Play.configuration.getProperty("application.baseDomain");
            System.out.println(request.host + ">>>>>");
            if (request.host == null || request.host.indexOf(baseDomain) >= 0) {
                response.setCookie("identity", session.getId(), "365d");
            } else {
                response.setCookie("identity", session.getId(), "." + baseDomain, "/", 360000, false);
            }
        }
    }

    @Before
    public static void setUser() {
        User user = getUser();
        Cas cas = new Cas();
        if (user != null) {
            cas.isLogin = true;
            cas.loginName = user.loginName;
            cas.user = user;
        }
        renderArgs.put("cas", cas);
    }

    public static User getUser() {
        String username = session.get(SESSION_USER_KEY);
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        if (Cache.get(SESSION_USER_KEY + username) == null) {
            return null;
        }
        User u = User.find("byLoginName", username).first();
        if (u == null) {
            u = User.find("byMobile", username).first();
        }
        return u;
    }

    /**
     * Action for the login route. We simply redirect to CAS login page.
     *
     * @throws Throwable
     */
    public static void login() throws Throwable {
        // We must avoid infinite loops after success authentication
        if (!Router.route(request).action.equals("modules.website.cas.SecureCAS.login")) {
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
        Cache.delete(SESSION_USER_KEY + username);

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
        CASUser casUser = null;
        if (ticket != null) {
            Logger.debug("[SecureCAS]: Try to validate ticket " + ticket);
            casUser = CASUtils.valideCasTicket(ticket);
            if (casUser != null) {
                isAuthenticated = Boolean.TRUE;
                session.put(SESSION_USER_KEY, casUser.getUsername());
                Cache.add(SESSION_USER_KEY + casUser.getUsername(), Boolean.TRUE);
                // we invoke the implementation of onAuthenticate
                Security.invoke("onAuthenticated", casUser);
            }
        }

        if (isAuthenticated) {
            // 记录登录IP及时间
            User user = getUser();
            if (user == null) {
                Logger.error("CAS check failed! username=(" + casUser.getUsername() + ")");
                fail();
            }
            user.loginIp = request.remoteAddress;
            user.save();

            UserLoginHistory history = new UserLoginHistory();
            history.user = user;
            history.loginAt = new Date();
            history.loginIp = request.remoteAddress;
            history.applicationName = Play.configuration.getProperty("application.name");
            history.sessionId = session.getId();
            history.save();

            // we redirect to the original URL
            String url = (String) Cache.get("url_" + session.getId());
            Cache.delete("url_" + session.getId());
            if (url == null) {
                url = "/";
            }
            Logger.debug("[SecureCAS]: redirect to url -> " + url);
            redirect(url);
        } else {
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
    @Before(unless = {"login", "logout", "fail", "authenticate", "pgtCallBack"})
    public static void filter() throws Throwable {
        Logger.debug("[SecureCAS]: CAS Filter for URL -> " + request.url);

        Logger.debug("session contains=" + session.contains(SESSION_USER_KEY) + ", value=" + session.get(SESSION_USER_KEY));
        if (skipCAS()) {
            Logger.debug("[SecureCAS]: Skip the CAS.");
            return;
        }

        // if user is authenticated, the username is in session !
        // Single Sign Out: 如果Cache.get(SESSION_USER_KEY + session.get(SESSION_USER_KEY))为空，则已经被其它应用注销.
        if (!session.contains(SESSION_USER_KEY)
                || (session.contains(SESSION_USER_KEY) && Cache.get(SESSION_USER_KEY + session.get(SESSION_USER_KEY)) == null)
                ) {
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
