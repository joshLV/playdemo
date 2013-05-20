package operate.rbac;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.operator.OperateNavigation;
import models.operator.OperateUser;
import play.Logger;
import play.Play;
import play.mvc.Http.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationHandler {

    private static Map<String, Menu> namedMenus;
    private static ThreadLocal<MenuContext> menuContext = new ThreadLocal<MenuContext>();

    static ThreadLocal<List<ContextedMenu>> topMenus = new ThreadLocal<>();
    static ThreadLocal<List<ContextedMenu>> secondLevelMenus = new ThreadLocal<>();
    static ThreadLocal<List<String>> stackMenuNames = new ThreadLocal<>();

    public static void initContextMenu(String applicationName, String activeNavigationName, OperateUser user) {
        initStackMenuNamesThreadLocal(applicationName, activeNavigationName, user);
        initSecondLevelMenusThreadLocal(applicationName, activeNavigationName, user);
        initTopMenusThreadLocal(applicationName, activeNavigationName, user);
    }


    private static void initStackMenuNamesThreadLocal(final String applicationName, final String activeNavigationName, final OperateUser user) {
        List<String> tmpStackMenuNames = CacheHelper.getCache(
                CacheHelper.getCacheKey(
                        new String[]{
                                OperateUser.CACHEKEY,
                                applicationName, activeNavigationName,
                                OperateUser.CACHEKEY + user.id
                        }, "STACK_MENU_NAMES"),
                new CacheCallBack<List<String>>() {
                    @Override
                    public List<String> loadData() {
                        Logger.info("initStackMenuNamesThreadLocal(%s, %s, %d)", applicationName, activeNavigationName, user.id);
                        List<OperateNavigation> navigateionStackList = OperateNavigation
                                .getNavigationParentStack(applicationName, activeNavigationName);
                        if (navigateionStackList == null) {
                            return new ArrayList<>();
                        }
                        List<String> navigationNameStackSets = new ArrayList<>();
                        for (OperateNavigation nav : navigateionStackList) {
                            navigationNameStackSets.add(nav.name);
                        }
                        return navigationNameStackSets;
                    }
                });
        stackMenuNames.set(tmpStackMenuNames);
    }


    private static void initSecondLevelMenusThreadLocal(final String applicationName, final String activeNavigationName, final OperateUser user) {
        List<ContextedMenu> tmpSecondLevelMenus = getSecondLevelMenulsThreadLocal(applicationName, activeNavigationName, user);
        secondLevelMenus.set(tmpSecondLevelMenus);
    }

    private static List<ContextedMenu> getSecondLevelMenulsThreadLocal(final String applicationName, final String activeNavigationName, final OperateUser user) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(
                        new String[]{
                                OperateUser.CACHEKEY,
                                applicationName, activeNavigationName,
                                OperateUser.CACHEKEY + user.id
                        }, "2_LEVEL_MENU"), new CacheCallBack<List<ContextedMenu>>() {
            @Override
            public List<ContextedMenu> loadData() {
                return getSecondLevelMenulsThreadLocalWithoutCache(applicationName, activeNavigationName, user);
            }
        });
    }

    private static List<ContextedMenu> getSecondLevelMenulsThreadLocalWithoutCache(String applicationName, String activeNavigationName, OperateUser user) {
        Logger.info("initSecondLevelMenusThreadLocal(%s, %s, %d)", applicationName, activeNavigationName, user.id);
        List<OperateNavigation> secondLevelNavigations = OperateNavigation
                .getSecondLevelNavigations(applicationName, activeNavigationName);
        if (secondLevelNavigations == null) {
            return new ArrayList<>();
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
        return _secondLevelMenus;
    }


    private static void initTopMenusThreadLocal(final String applicationName, final String activeNavigationName, final OperateUser user) {
        List<ContextedMenu> tmpTopMenu = CacheHelper.getCache(
                CacheHelper.getCacheKey(
                        new String[]{OperateUser.CACHEKEY,
                                applicationName, activeNavigationName,
                                OperateUser.CACHEKEY + user.id},
                        "TOPMENUS4"),
                new CacheCallBack<List<ContextedMenu>>() {
                    @Override
                    public List<ContextedMenu> loadData() {
                        Logger.info("initTopMenusThreadLocal(%d)", user.id);
                        List<OperateNavigation> topNavigations = OperateNavigation.getTopNavigations();
                        List<ContextedMenu> _topMenus = new ArrayList<>();
                        for (OperateNavigation navigation : topNavigations) {
                            if (navigation.permissions == null ||
                                    navigation.permissions.size() == 0 ||
                                    ContextedPermission.hasPermissions(navigation.permissions)) {
                                Menu menu = Menu.from(navigation);

                                ContextedMenu contextedMenu = new ContextedMenu(menu,
                                        getMenuContext());

                                List<ContextedMenu> secondLevelMenuls = getSecondLevelMenulsThreadLocalWithoutCache(navigation.applicationName, navigation.name, user);

                                contextedMenu.setChildren(secondLevelMenuls);
                                _topMenus.add(contextedMenu);
                            }
                        }
                        return _topMenus;
                    }
                });
        topMenus.set(tmpTopMenu);
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
        return new MenuContext(Request.current(), stackMenuNames.get());
    }


    public static List<ContextedMenu> getTopMenus() {
        return topMenus.get();
    }

    public static List<ContextedMenu> getSecondLevelMenus() {
        return secondLevelMenus.get();
    }

}
