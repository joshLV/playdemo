package controllers;

import java.util.HashSet;
import java.util.Set;
import models.admin.OperateNavigation;
import models.admin.OperatePermission;
import models.admin.OperateRole;
import models.admin.OperateUser;
import navigation.ContextedPermission;
import navigation.NavigationHandler;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http.Request;
import controllers.operate.cas.SecureCAS;

/**
 * Have a menu automatically injected in your renderArgs
 *
 * This MenuInjector has an @Before annotated action that reads comma separated menu names from the navigation.defaultMenus
 * configuration parameter in your conf/application.conf file, and injects those menus into your renderArgs.
 */
public class MenuInjector extends Controller {

    private static ThreadLocal<OperateUser> _user = new ThreadLocal<>();

    public static void injectDefaultMenus() {
        for(String menuName : Play.configuration.getProperty("navigation.defaultMenus", "main").toString().split(",")) {
            if(!StringUtils.isBlank(menuName)) {
                renderArgs.put(menuName + "Menu", NavigationHandler.getMenu(StringUtils.trim(menuName)));
            }
        }
    }

    @Before
    public static void injectCurrentMenus() {
        if (request.invokedMethod == null)
            return;

        String userName = session.get(SecureCAS.SESSION_USER_KEY);

        Logger.info("================================================================ currentUser = " + userName);

        OperateUser user = null;
        // 检查权限
        if (userName != null) {
            // 查出当前用户的所有权限
            user = OperateUser.findUser(userName);
            Logger.info(" ---------------------------- user : " + user);
            if (user != null && user.roles != null) {
                Logger.info("user.id = " + user.id + ", name=" + user.loginName);
                Logger.info("get role " + user.roles);
                for (OperateRole role : user.roles) {
                    Logger.info("user.role=" + role.key);
                }
            }
            _user.set(user);
            ContextedPermission.init(user);
        }

        if (user == null) {
            error(403, "没有登录，请考虑合并SecureCAS和这个类，看上去@With加进去的类是没有顺序的");
        }

        // 得到当前菜单的名字
        String currentMenuName = getCurrentMenuName();

        // 检查权限
        checkRight(currentMenuName);

        String applicationName = Play.configuration.getProperty("application.name");
        NavigationHandler.initContextMenu(applicationName, currentMenuName);

        renderArgs.put("topMenus", NavigationHandler.getTopMenus());
        renderArgs.put("secondLevelMenu", NavigationHandler.getSecondLevelMenus());
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
     * @param methodNavigation
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
                Logger.info(" 当前菜单(" + currentMenuName + ")的权限是：" + perm.key);
                rightSet.add(perm.key);
            }
        }

        Logger.info("???????? current permission = " + ContextedPermission.getAllowPermissions() + ", url=" + Request.current().url);
        if (ContextedPermission.getAllowPermissions() != null) {
            for (String s : ContextedPermission.getAllowPermissions()) {
                Logger.info("   perm:" + s);
            }
        }
        for (String r : rightSet) {
            Logger.info("   right:" + r);
        }

        if (rightSet.size() > 0) {
            boolean hasRight = ContextedPermission.hasPermissionKeys(rightSet);
            if (!hasRight) {
                error(403, "没有权限访问.");
            }
        } // else 如果没有加上Right标注，不检查权限
    }
}