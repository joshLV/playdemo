package controllers;

import controllers.operate.cas.Security;
import models.operator.OperateNavigation;
import models.operator.OperatePermission;
import models.operator.OperateRole;
import models.operator.OperateUser;
import models.operator.OperateUserLoginHistory;
import operate.rbac.ContextedPermission;
import operate.rbac.NavigationHandler;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.operate.cas.CASUtils;
import play.operate.cas.models.CASUser;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Have a menu automatically injected in your renderArgs
 *
 * This MenuInjector has an @Before annotated action that reads comma separated menu names from the navigation.defaultMenus
 * configuration parameter in your conf/application.conf file, and injects those menus into your renderArgs.
 */
public class OperateRbac extends Controller {

    private static ThreadLocal<OperateUser> _user = new ThreadLocal<>();


    public static final String SESSION_USER_KEY = "operate_login";


    /**
     * Action for the login route. We simply redirect to CAS login page.
     *
     * @throws Throwable
     */
    public static void login() throws Throwable {
        // We must avoid infinite loops after success authentication
        if (!Router.route(request).action.equals("OperateRbac.login")) {
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
            Logger.debug("[OperateCAS]: Try to validate ticket " + ticket);
            casUser = CASUtils.valideCasTicket(ticket);
            if (casUser != null) {
                isAuthenticated = Boolean.TRUE;
                session.put(SESSION_USER_KEY, casUser.getUsername());
                Cache.safeAdd(SESSION_USER_KEY + casUser.getUsername(), Boolean.TRUE, "60mn");
                // we invoke the implementation of onAuthenticate
                Security.onAuthenticated(casUser);
            }
        }

        if (isAuthenticated) {
            // 登录记录
            OperateUser user = OperateUser.findUser(casUser.getUsername());
            if (user != null) {
                user.lastLoginIP = request.remoteAddress;
                user.save();
                
                OperateUserLoginHistory history = new OperateUserLoginHistory();
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
            Logger.debug("[OperateCAS]: redirect to url -> " + url);
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

    /**
     * Method that do CAS Filter and check rights.
     *
     * @throws Throwable
     */
    @Before(unless = { "login", "logout", "fail", "authenticate", "pgtCallBack", "setLoginUserForTest" })
    public static void injectCurrentMenus() {
        Logger.debug("[OperateCAS]: CAS Filter for URL -> " + request.url);

        if (Security.isTestLogined()) {
            session.put(SESSION_USER_KEY, Security.getLoginUserForTest());
        }
        
        if (request.invokedMethod == null)
            return;

        String userName = session.get(SESSION_USER_KEY);

        Logger.debug("======================================== currentUser = " + userName);

        OperateUser user = null;
        
        List<OperateRole> testRoles = OperateRole.findAll();
        for (OperateRole role : testRoles) {
            Logger.debug("all: role.id=%d, role.key=%s, loadversion=%d", role.id, role.key, role.loadVersion);
        }
        
        // 检查权限
        if (userName != null && Cache.get(SESSION_USER_KEY + userName) != null) {
            // 查出当前用户的所有权限
            user = OperateUser.findUser(userName);
            Logger.debug(" ---------------------------- user : " + user);
            if (user != null) {
                renderArgs.put("currentUser", user);
            }
            if (Logger.isDebugEnabled() && user != null && user.roles != null) {
                Logger.debug("user.id = " + user.id + ", name=" + user.loginName);
                Logger.debug("get role " + user.roles);
                for (OperateRole role : user.roles) {
                    Logger.debug("user.role=%s, rold.id=%d", role.key, role.id);
                }
            }
            _user.set(user);
            ContextedPermission.init(user);
        }

        if (user == null) {
            Logger.debug("[OperateCAS]: user is not authenticated");
            // we put into cache the url we come from
            Cache.add("url_" + session.getId(), request.method == "GET" ? request.url : "/", "10min");

            // we redirect the user to the cas login page
            String casLoginUrl = CASUtils.getCasLoginUrl(true);
            redirect(casLoginUrl);            
        }

        Cache.safeAdd(SESSION_USER_KEY + userName, Boolean.TRUE, "60mn");

        // 得到当前菜单的名字
        String currentMenuName = getCurrentMenuName();
        
        String applicationName = Play.configuration.getProperty("application.name");
        NavigationHandler.initContextMenu(applicationName, currentMenuName);

        renderArgs.put("topMenus", NavigationHandler.getTopMenus());
        renderArgs.put("secondLevelMenu", NavigationHandler.getSecondLevelMenus());
        // 检查权限
        checkRight(currentMenuName);
    }

    public static void injectDefaultMenus() {
        for(String menuName : Play.configuration.getProperty("navigation.defaultMenus", "main").toString().split(",")) {
            if(!StringUtils.isBlank(menuName)) {
                renderArgs.put(menuName + "Menu", NavigationHandler.getMenu(StringUtils.trim(menuName)));
            }
        }
    }

    @Finally
    public static void cleanPermission() {
        ContextedPermission.clean();
        _user.set(null);
    }

    public static OperateUser currentUser() {
        return _user.get();
    }


    /**
     * 得到当前菜单的名字。
     * 从Controller或method上检查 {@link ActiveNavigation} 标注，取其value为当前菜单名。
     * 优先使用Method上的名字，只能有一个值。
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

        OperateNavigation currentNavigation = OperateNavigation.findByName(currentMenuName);
        // 把当前菜单上的权限也作为检查点，这样一个方法只需要指定@ActiveNavigation，就不需要再指定@Right了
        if (currentNavigation != null && currentNavigation.permissions != null) {
            for (OperatePermission perm : currentNavigation.permissions) {
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
}
