package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.admin.SupplierNavigation;
import navigation.NavigationHandler;
import navigation.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

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
        NavigationHandler.initContextMenu(currentMenuName);
        renderArgs.put("topMenus", NavigationHandler.getTopMenus());
        renderArgs.put("secendLevelMenu", NavigationHandler.getSecendLevelMenus());
    }
}