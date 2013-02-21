package operate.rbac;

import models.operator.OperateNavigation;
import play.Play;
import play.mvc.Http.Request;

import java.util.*;

public class NavigationHandler {

    private static Map<String, Menu> namedMenus;
    private static ThreadLocal<MenuContext> menuContext = new ThreadLocal<MenuContext>();

    static ThreadLocal<List<ContextedMenu>> topMenus = new ThreadLocal<>();
    static ThreadLocal<List<ContextedMenu>> secondLevelMenus = new ThreadLocal<>();
    static ThreadLocal<Set<String>> stackMenuNames = new ThreadLocal<>();

    public static void initContextMenu(String applicationName, String activeNavigationName) {
        initStackMenuNamesThreadLocal(applicationName, activeNavigationName);
        initSecondLevelMenusThreadLocal(applicationName, activeNavigationName);
        initTopMenusThreadLocal();
    }


    private static void initStackMenuNamesThreadLocal(String applicationName, String activeNavigationName) {
        List<OperateNavigation> navigateionStackList = OperateNavigation
                .getNavigationParentStack(applicationName, activeNavigationName);
        if (navigateionStackList == null) {
            return;
        }
        Set<String> navigationNameStackSets = new HashSet<>();
        for (OperateNavigation nav : navigateionStackList) {
            navigationNameStackSets.add(nav.name);
        }
        stackMenuNames.set(navigationNameStackSets);
    }


    private static void initSecondLevelMenusThreadLocal(String applicationName, String activeNavigationName) {
        List<OperateNavigation> secondLevelNavigations = OperateNavigation
                .getSecondLevelNavigations(applicationName, activeNavigationName);
        if (secondLevelNavigations == null) {
            return;
        }
        List<ContextedMenu> _secondLevelMenus = new ArrayList<>();
        for (OperateNavigation navigation : secondLevelNavigations) {
            if (navigation.permissions == null ||
                    navigation.permissions.size() == 0 ||
                    ContextedPermission.hasPermissions(navigation.permissions)) {
                Menu menu = Menu.from(navigation);
                ContextedMenu contextedMenu = new ContextedMenu(menu, getMenuContext());
                _secondLevelMenus.add(contextedMenu);
            }
        }
        secondLevelMenus.set(_secondLevelMenus);
    }


    private static void initTopMenusThreadLocal() {
        List<OperateNavigation> topNavigations = OperateNavigation.getTopNavigations();
        List<ContextedMenu> _topMenus = new ArrayList<>();
        for (OperateNavigation navigation : topNavigations) {
            if (navigation.permissions == null ||
                    navigation.permissions.size() == 0 ||
                    ContextedPermission.hasPermissions(navigation.permissions)) {
                Menu menu = Menu.from(navigation);
                ContextedMenu contextedMenu = new ContextedMenu(menu,
                        getMenuContext());
                _topMenus.add(contextedMenu);
            }
        }
        topMenus.set(_topMenus);
    }

    /// ================ ================

    /**
     * 把数据库中的所有Navigation转化为菜单.
     */
    public static void initNamedMenus() {
        namedMenus = new HashMap<>();
        List<OperateNavigation> allNavigations = OperateNavigation.findAll();
        for (OperateNavigation nav : allNavigations) {
            Menu menu = Menu.from(nav);
            namedMenus.put(menu.menuKey(), menu);
        }
    }

    public static ContextedMenu getMenu(String name) {
        Menu menu = namedMenus.get(name);
        if (menu == null) {
            String fullName = Play.configuration.getProperty("application.name") + "." + name;
            menu = namedMenus.get(fullName);
        }
        if (menu == null) {
            throw new IllegalArgumentException("Menu '" + name + "' not defined.");
        }
        return new ContextedMenu(menu, getMenuContext());
    }

    public static void clearMenuContext() {
        menuContext.set(null);
        topMenus.set(null);
        secondLevelMenus.set(null);
        stackMenuNames.set(null);
    }

    public static MenuContext getMenuContext() {
        MenuContext context = menuContext.get();
        if (context == null) {
            context = buildMenuContext();
            menuContext.set(context);
        }
        return context;
    }

    protected static MenuContext buildMenuContext() {
        MenuContext menuContext = new MenuContext(Request.current(), stackMenuNames.get());
        return menuContext;
    }


    public static List<ContextedMenu> getTopMenus() {
        return topMenus.get();
    }

    public static List<ContextedMenu> getSecondLevelMenus() {
        return secondLevelMenus.get();
    }

}
