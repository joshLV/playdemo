package controllers;

import navigation.ContextedPermission;
import navigation.NavigationHandler;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import controllers.supplier.cas.SecureCAS;

/**
 * Have a menu automatically injected in your renderArgs
 *
 * This MenuInjector has an @Before annotated action that reads comma separated menu names from the navigation.defaultMenus
 * configuration parameter in your conf/application.conf file, and injects those menus into your renderArgs.
 */
public class MenuInjector extends Controller {

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
        
        // 检查权限
        if (userName != null) {
            ContextedPermission.init(userName);
        }
        checkRight();
        
        // 得到当前菜单的名字
        String currentMenuName = getCurrentMenuName();
        String applicationName = Play.configuration.getProperty("application.name");
        NavigationHandler.initContextMenu(applicationName, currentMenuName);
        renderArgs.put("topMenus", NavigationHandler.getTopMenus());
        renderArgs.put("secondLevelMenu", NavigationHandler.getSecondLevelMenus());
    }
    
    @Finally
    public static void cleanPermission() {
	    ContextedPermission.clean();
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
    
    private static void checkRight() {
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
        
        if (rights != null) {
            boolean hasRight = ContextedPermission.hasPermissions(rights);
            if (!hasRight) {
                error(403, "没有权限访问.");
            }
        } // else 如果没有加上Right标注，不检查权限
    }
}