package navigation;

import models.admin.SupplierNavigation;
import play.Logger;
import play.Play;
import play.mvc.Http.Request;
import play.supplier.cas.CASUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<SupplierNavigation> navigateionStackList = SupplierNavigation
                .getNavigationParentStack(applicationName, activeNavigationName);
        if (navigateionStackList == null) {
            return;
        }
        Set<String> navigationNameStackSets = new HashSet<>();
        for (SupplierNavigation nav : navigateionStackList) {
            navigationNameStackSets.add(nav.name);
        }
        stackMenuNames.set(navigationNameStackSets);
    }


    private static void initSecondLevelMenusThreadLocal(String applicationName, String activeNavigationName) {
        List<SupplierNavigation> secondLevelNavigations = SupplierNavigation
                .getSecondLevelNavigations(applicationName, activeNavigationName);
        if (secondLevelNavigations == null) {
            return;
        }
        List<ContextedMenu> _secondLevelMenus = new ArrayList<>();
        for (SupplierNavigation navigation : secondLevelNavigations) {
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
        List<SupplierNavigation> topNavigations = SupplierNavigation.getTopNavigations();
        List<ContextedMenu> _topMenus = new ArrayList<>();
        for (SupplierNavigation navigation : topNavigations) {
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
        List<SupplierNavigation> allNavigations = SupplierNavigation.findAll();
        for (SupplierNavigation nav : allNavigations) {
            Menu menu = Menu.from(nav);
            namedMenus.put(menu.menuKey(), menu);
        }
    }

    public static ContextedMenu getMenu(String name) {
        Menu menu = namedMenus.get(name);
        for (String key : namedMenus.keySet()) {
            Logger.info("   key=" + key + ", value=" + namedMenus.get(key));
        }
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

    // 用于显示导航
    public static String getOperatorProfileUrl() {
        String baseUrl = Play.configuration.getProperty("application.baseUrl");

        String hostUrl = baseUrl.replaceAll("\\{domain\\}\\.[^\\.]+", CASUtils.getSubDomain() + ".admin");

        return hostUrl + "/profile";
    }

    // 用于显示导航
    public static String getSupplierInfoUrl() {
        String baseUrl = Play.configuration.getProperty("application.baseUrl");

        String hostUrl = baseUrl.replaceAll("\\{domain\\}\\.[^\\.]+", CASUtils.getSubDomain() + ".home");

        return hostUrl + "/info";
    }

}
