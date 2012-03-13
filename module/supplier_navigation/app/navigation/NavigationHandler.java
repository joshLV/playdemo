package navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.admin.SupplierNavigation;
import play.Play;
import play.mvc.Http.Request;

public class NavigationHandler {

    private static Map<String, Menu> namedMenus;
    private static ThreadLocal<MenuContext> menuContext = new ThreadLocal<MenuContext>();

    static ThreadLocal<List<ContextedMenu>> topMenus = new ThreadLocal<>();
    static ThreadLocal<List<ContextedMenu>> secendLevelMenus = new ThreadLocal<>();
    static ThreadLocal<Set<String>> stackMenuNames = new ThreadLocal<>();

    public static void initContextMenu(String applicationName, String activeNavigationName) {
        System.out.println("activeNavName=" + activeNavigationName);
        initStackMenuNamesThreadLocal(applicationName, activeNavigationName);
        initSecendLevelMenusThreadLocal(applicationName, activeNavigationName);
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


    private static void initSecendLevelMenusThreadLocal(String applicationName, String activeNavigationName) {
        List<SupplierNavigation> secendLevelNavigations = SupplierNavigation
                .getSecendLevelNavigations(applicationName, activeNavigationName);
        if (secendLevelNavigations == null) {
            return;
        }
        List<ContextedMenu> _secendLevelMenus = new ArrayList<>();
        for (SupplierNavigation topNavitagion : secendLevelNavigations) {
            Menu menu = Menu.from(topNavitagion);
            ContextedMenu contextedMenu = new ContextedMenu(menu, getMenuContext());
            _secendLevelMenus.add(contextedMenu);
        }
        secendLevelMenus.set(_secendLevelMenus);
    }


    private static void initTopMenusThreadLocal() {
        List<SupplierNavigation> topNavigations = SupplierNavigation.getTopNavigations();
        List<ContextedMenu> _topMenus = new ArrayList<>();
        for (SupplierNavigation topNavitagion : topNavigations) {
            Menu menu = Menu.from(topNavitagion);
            ContextedMenu contextedMenu = new ContextedMenu(menu, getMenuContext());

            _topMenus.add(contextedMenu);
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
        if (menu == null) {
            String fullName = Play.configuration.getProperty("application.name") + "." + name;
            menu = namedMenus.get(fullName);
        }
        if(menu == null) {
            throw new IllegalArgumentException("Menu '" + name + "' not defined.");
        }
        return new ContextedMenu(menu, getMenuContext());
    }

    public static void clearMenuContext() {
        menuContext.set(null);
        topMenus.set(null);
        secendLevelMenus.set(null);
        stackMenuNames.set(null);
    }

    public static MenuContext getMenuContext() {
        MenuContext context = menuContext.get();
        if(context == null) {
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

    public static List<ContextedMenu> getSecendLevelMenus() {
        return secendLevelMenus.get();
    }
}
