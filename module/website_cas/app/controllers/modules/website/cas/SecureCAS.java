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

import cache.CacheHelper;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.consumer.OpenIdSource;
import models.consumer.User;
import models.consumer.UserLoginHistory;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.modules.website.cas.CASUtils;
import play.modules.website.cas.annotation.Check;
import play.modules.website.cas.models.CASUser;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Http.Header;

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

        Logger.debug("cookieId:" + cookieId);

        if (cookieId == null) {
            Logger.debug("[SecureCAS]: set a new cookie identity");
            String baseDomain = Play.configuration.getProperty("application.baseDomain");
            if (request.host == null || !request.host.contains(baseDomain)) {
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
            cas.loginName = user.getShowName();
            cas.user = user;
        }
        renderArgs.put("cas", cas);
    }

    /**
     * oauth用户的样例: UserProfile#SinaWeibo:1802362721
     *
     * @return
     */
    public static User getUser() {
        String userIdentity = session.get(SESSION_USER_KEY);
        if (StringUtils.isEmpty(userIdentity)) {
            return null;
        }
        if (Cache.get(SESSION_USER_KEY + userIdentity) == null) {
            return null;
        }

        User u = User.find("byLoginName", userIdentity).first();
        if (u == null) {
            u = User.find("byMobile", userIdentity).first();
            if (u == null) {
                //猜测是oauth用户
                OpenIdSource openIdSource = getOpenIdSourceFromUserIdentity(userIdentity);
                if (openIdSource != null && !openIdSource.name().equals("0")) {
                    String openId = getOpenIdFromUserIdentity(userIdentity);
                    Logger.debug("openId:" + openId);
                    Logger.debug("openIdSource.value():" + openIdSource.name());

                    u = User.find("byOpenIdSourceAndOpenId", openIdSource, openId).first();
                }
            }
        }

        return u;
    }

    private static String getOpenIdFromUserIdentity(String userIdentity) {
        final int location = userIdentity.indexOf(':');
        if (location < 0) {
            return null;
        }

        return userIdentity.substring(location + 1);
    }

    private static OpenIdSource getOpenIdSourceFromUserIdentity(String userIdentity) {
        final int location = userIdentity.indexOf(':');
        if (location < 0) {
            return null;
        }
        OpenIdSource openIdSource = null;
        try {
            openIdSource = OpenIdSource.valueOf(userIdentity.substring("UserProfile#".length(), location));
        } catch (Exception e) {
            //ignore
        }
        return openIdSource;
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
        } else {
            Header header = request.headers.get("referer");
            if (header != null) {
                String referer = header.value();
                Cache.add("url_" + session.getId(), request.method == "GET" ? referer : "/", "10min");
            }
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
        // forbidden();
        // 如果失败，直接到logout先
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        redirect(casLogoutUrl);
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

        Logger.debug("              ticket:" + ticket);
        if (ticket != null) {
            Logger.debug("[SecureCAS]: Try to validate ticket " + ticket);
            casUser = CASUtils.valideCasTicket(ticket);
            if (casUser != null) {
                isAuthenticated = Boolean.TRUE;

                String username = casUser.getUsername().replaceAll("\\s+", "");
                session.put(SESSION_USER_KEY, username);
                Cache.safeAdd(SESSION_USER_KEY + username, Boolean.TRUE, "1h");
                // we invoke the implementation of onAuthenticate
                Security.invoke("onAuthenticated", casUser);

            }
        }

        if (isAuthenticated) {
            // 记录登录IP及时间
            User user = getUser();
            String username = casUser.getUsername().replaceAll("\\s+", "");
            if (user == null) {
                Logger.error("CAS check failed! username=(" + username + ")");
                fail();
                return;
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
        Logger.info("[SecureCAS]: CAS Filter for URL -> " + request.url + ", test=" + Security.isTestLogined());

        // 测试用，见 @Security.setLoginUserForTest说明
        if (Security.isTestLogined()) {
            Logger.debug("set test user %s", Security.getLoginUserForTest());
            session.put(SESSION_USER_KEY, Security.getLoginUserForTest());
        }

        Logger.info("session contains=" + session.contains(SESSION_USER_KEY) + ", value=" + session.get(SESSION_USER_KEY));
        if (skipCAS()) {
            Logger.info("[SecureCAS]: Skip the CAS.");
            return;
        }

        // if user is authenticated, the username is in session !
        // Single Sign Out: 如果Cache.get(SESSION_USER_KEY + session.get(SESSION_USER_KEY))为空，则已经被其它应用注销.
        if (getUser() == null) {
            Logger.info("[SecureCAS]: user is not authenticated");
            // we put into cache the url we come from
            Cache.add("url_" + session.getId(), request.method.equals("GET") ? request.url : "/", "10min");

            TargetOAuth targetOAuth = getControllerAnnotation(TargetOAuth.class);
            if (targetOAuth != null) {
                if (OAuthType.SINA == targetOAuth.value()) {
                    redirect(CASUtils.getSinaOAuthLoginUrl());
                }
            }

            // we redirect the user to the cas login page
            String casLoginUrl = CASUtils.getCasLoginUrl(true);
            redirect(casLoginUrl);
        }
        // 重新确保cache不会过期.
        Cache.safeAdd(SESSION_USER_KEY + session.get(SESSION_USER_KEY), Boolean.TRUE, "1h");
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

    @After
    public static void cleanCacheHelper() {
        CacheHelper.cleanPreRead();
    }
}
