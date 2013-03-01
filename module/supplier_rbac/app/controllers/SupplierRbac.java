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
package controllers;

import cache.CacheHelper;
import controllers.supplier.cas.Security;
import models.admin.SupplierNavigation;
import models.admin.SupplierPermission;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.admin.SupplierUserLoginHistory;
import navigation.ContextedPermission;
import navigation.NavigationHandler;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.supplier.cas.CASUtils;
import play.supplier.cas.models.CASUser;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a part of the play module secure-cas. It add the ability to check if the user have access to the
 * request. If the user is note logged, it redirect the user to the CAS login page and authenticate it.
 *
 * @author bsimard
 */
public class SupplierRbac extends Controller {

    public static final String SESSION_USER_KEY = "supplier_login";

    private static ThreadLocal<SupplierUser> _user = new ThreadLocal<>();

    public static void injectDefaultMenus() {
        for (String menuName : Play.configuration.getProperty("navigation.defaultMenus", "main").toString().split(",")) {
            if (!StringUtils.isBlank(menuName)) {
                renderArgs.put(menuName + "Menu", NavigationHandler.getMenu(StringUtils.trim(menuName)));
            }
        }
    }

    /**
     * Method that do CAS Filter and check rights.
     *
     * @throws Throwable
     */
    @Before(unless = {"login", "logout", "fail", "authenticate", "pgtCallBack", "setLoginUserForTest"})
    public static void filterRbac() {
        Logger.debug("[SupplierRbac]: CAS Filter for URL -> " + request.url);

        // 测试用，见 @Security.setLoginUserForTest说明
        if (Security.isTestLogined()) {
            session.put(SESSION_USER_KEY, Security.getLoginUserForTest());
        }

        if (request.invokedMethod == null)
            return;

        String userName = getDomainUserName(session.get(SESSION_USER_KEY));
        String subDomain = CASUtils.getSubDomain();

        Logger.info(" currentUser = " + userName + ", domain=" + subDomain
                + ", cache=" + Cache.get(SESSION_USER_KEY + userName));

        SupplierUser user = null;
        // 检查权限
        // Single Sign Out: 如果Cache.get(SESSION_USER_KEY + userName)为空，则已经被其它应用注销.
        if (userName != null && Cache.get(SESSION_USER_KEY + userName) != null) {
            // 查出当前用户的所有权限
            user = SupplierUser.findUserByDomainName(subDomain, userName);
            Logger.debug(" ---------------------------- user : " + user);
            if (user != null) {
                renderArgs.put("currentUser", user);
            }
            if (Logger.isDebugEnabled() && user != null && user.roles != null) {
                Logger.debug("user.id = " + user.id + ", name=" + user.loginName);
                Logger.debug("get role " + user.roles);
                for (SupplierRole role : user.roles) {
                    Logger.debug("user.role=" + role.key);
                }
            }
            _user.set(user);
            ContextedPermission.init(user);
        }

        if (user == null) {
            Logger.info("[SupplierRbac]: user is not authenticated");
            // we put into cache the url we come from
            Cache.add("url_" + session.getId(), request.method == "GET" ? request.url : "/", "10min");

            // we redirect the user to the cas login page
            String casLoginUrl = CASUtils.getCasLoginUrl(true);
            redirect(casLoginUrl);
        }

        Cache.safeAdd(SESSION_USER_KEY + userName, Boolean.TRUE, "120mn");

        // 得到当前菜单的名字
        String currentMenuName = getCurrentMenuName();

        String applicationName = Play.configuration.getProperty("application.name");
        NavigationHandler.initContextMenu(applicationName, currentMenuName);

        renderArgs.put("topMenus", NavigationHandler.getTopMenus());
        renderArgs.put("secondLevelMenu", NavigationHandler.getSecondLevelMenus());
        renderArgs.put("operatorProfileUrl", NavigationHandler.getOperatorProfileUrl());
        renderArgs.put("supplierInfoUrl", NavigationHandler.getSupplierInfoUrl());
        renderArgs.put("supplierCompany", user.supplier);
        if (user.shop != null) {
            renderArgs.put("supplierShop", user.shop.name);
        }
        // 检查权限
        checkRight(currentMenuName);
    }

    @Finally
    public static void cleanPermission() {
        ContextedPermission.clean();
        _user.set(null);
    }

    public static SupplierUser currentUser() {
        return _user.get();
    }

    public static String getDomainUserName(String fullUserName) {
        if (fullUserName == null) {
            return null;
        }
        if (!fullUserName.contains("@")) {
            return fullUserName.trim();
        }
        return fullUserName.split("@", 2)[0].trim();
    }


    /**
     * 得到当前菜单的名字。
     * 从Controller或method上检查 {@link ActiveNavigation} 标注，取其value为当前菜单名。
     * 优先使用Method上的名字，只能有一个值。
     *
     * @return
     */
    private static String getCurrentMenuName() {
        ActiveNavigation methodNavigation = getActionAnnotation(ActiveNavigation.class);
        String currentMenuName = null;
        if (methodNavigation != null) {
            currentMenuName = methodNavigation.value();
        } else {
            ActiveNavigation controllerNavigation = getControllerAnnotation(ActiveNavigation.class);
            if (controllerNavigation != null) {
                currentMenuName = controllerNavigation.value();
            }
        }
        return currentMenuName;
    }

    private static void checkRight(String currentMenuName) {
        Right methodRightAnnotation = getActionAnnotation(Right.class);
        String[] rights = null;
        if (methodRightAnnotation != null) {
            rights = methodRightAnnotation.value();
        } else {
            Right controllerRightAnnotation = getControllerAnnotation(Right.class);
            if (controllerRightAnnotation != null) {
                rights = controllerRightAnnotation.value();
            }
        }

        Set<String> rightSet = new HashSet<>();
        if (rights != null) {
            for (String r : rights) {
                rightSet.add(r);
            }
        }

        SupplierNavigation currentNavigation = SupplierNavigation.findByName(currentMenuName);
        // 把当前菜单上的权限也作为检查点，这样一个方法只需要指定@ActiveNavigation，就不需要再指定@Right了
        if (currentNavigation != null && currentNavigation.permissions != null) {
            for (SupplierPermission perm : currentNavigation.permissions) {
                Logger.debug(" 当前菜单(" + currentMenuName + ")的权限是：" + perm.key);
                rightSet.add(perm.key);
            }
        }

        Logger.debug("???????? current permission = " + ContextedPermission.getAllowPermissions() + ", url=" + Request.current().url);
        if (ContextedPermission.getAllowPermissions() != null) {
            for (String s : ContextedPermission.getAllowPermissions()) {
                Logger.debug("   perm:" + s);
            }
        }
        for (String r : rightSet) {
            Logger.debug("   right:" + r);
        }

        if (rightSet.size() > 0) {
            boolean hasRight = ContextedPermission.hasPermissionKeys(rightSet);
            if (!hasRight) {
                String message = "没有权限访问！";
                if (currentNavigation != null) {
                    message = "没有权限访问 <strong>" + currentNavigation.text + "</strong> 功能。";
                }
                renderTemplate("Defaults/index.html", message);
            }
        } // else 如果没有加上Right标注，不检查权限
    }

    /**
     * Action for the login route. We simply redirect to CAS login page.
     *
     * @throws Throwable
     */
    public static void login() throws Throwable {
        // We must avoid infinite loops after success authentication
        if (!Router.route(request).action.equals("SupplierRbac.login")) {
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

        Logger.info("SupplierRbac.logout username=[" + username + "]");
        // we clear cache
        Cache.delete("pgt_" + username);
        Cache.delete(SESSION_USER_KEY + username);

        // we clear session
        session.clear();

        // we invoke the implementation of "onDisconnected"
        Security.onDisconnected();

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
            Logger.info("[SupplierRbac]: Try to validate ticket " + ticket);
            casUser = CASUtils.valideCasTicket(ticket);
            if (casUser != null) {
                isAuthenticated = Boolean.TRUE;
                String username = casUser.getUsername().replaceAll("\\s+", "");
                Logger.info("casUser.getUsername()=[" + username + "]");
                session.put(SESSION_USER_KEY, username);
                Cache.safeAdd(SESSION_USER_KEY + getDomainUserName(username), Boolean.TRUE, "120mn");
                // we invoke the implementation of onAuthenticate
                Security.onAuthenticated(casUser);
            }
        }

        if (isAuthenticated) {
            // 登录记录
            String userName = getDomainUserName(casUser.getUsername());
            Logger.info("getDomainUserName=[" + userName + "]");
            String subDomain = CASUtils.getSubDomain();
            SupplierUser user = SupplierUser.findUserByDomainName(subDomain, userName);
            if (user != null) {
                user.lastLoginIP = request.remoteAddress;
                user.save();

                SupplierUserLoginHistory history = new SupplierUserLoginHistory();
                history.user = user;
                history.loginAt = new Date();
                history.loginIp = request.remoteAddress;
                history.applicationName = Play.configuration.getProperty("application.name");
                history.sessionId = session.getId();
                history.save();
            }

            // we redirect to the original URL
            String url = (String) Cache.get("url_" + session.getId());
            Cache.delete("url_" + session.getId());
            if (url == null) {
                url = "/";
            }
            Logger.debug("[SupplierRbac]: redirect to url -> " + url);
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

    @After
    public static void cleanCacheHelper() {
        CacheHelper.cleanPreRead();
    }
}
