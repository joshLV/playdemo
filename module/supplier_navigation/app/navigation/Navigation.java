package navigation;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.admin.SupplierNavigation;

import org.yaml.snakeyaml.Yaml;

import play.Play;
import play.mvc.Http.Request;
import play.vfs.VirtualFile;

/**
 * Keeper of the bare Menus
 *
 * This class holds a static reference to all the Menus. You can retrive a Menu from this class,
 * which will be automatically wrapped in a ContextedMenu, ready to be inserted into your view.
 */
public class Navigation {

    private static Map<String, Menu> namedMenus;
    private static ThreadLocal<MenuContext> menuContext = new ThreadLocal<MenuContext>();

    public static void init(VirtualFile file) {
        namedMenus = new HashMap<String, Menu>();
        loadFile(file);
    }

    public static void loadFile(VirtualFile file) {
        Yaml yaml = new Yaml();
        Object o = yaml.load(file.inputstream());

        long currentLoadVersion = System.currentTimeMillis();
        String applicationName = Play.configuration.getProperty("application.name");

        if (o instanceof LinkedHashMap<?, ?>) {
            Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");

            LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
            for (Object key : objects.keySet()) {
                Matcher matcher = keyPattern.matcher(key.toString().trim());
                if (matcher.matches()) {
                    String type = matcher.group(1);
                    String name = matcher.group(2);
                    if (!type.equals("Menu")) {
                        throw new RuntimeException("Navigation file contains invalid type " + type);
                    }

                    if(namedMenus.containsKey(name)) {
                        throw new RuntimeException("Navigation file contains a duplicate navigation item with name " + name);
                    }

                    Menu entry = new Menu();
                    entry.name = name;
                    Map fields = objects.get(key);

                    if (fields.containsKey("text")) {
                        entry.text = (String) fields.get("text");
                    }
                    if (fields.containsKey("action")) {
                        entry.action = (String) fields.get("action");
                    }
                    if (fields.containsKey("url")) {
                        entry.url = (String) fields.get("url");
                    }
                    if (fields.containsKey("params")) {
                        entry.params = (Map<String, String>) fields.get("params");
                    }
                    if (fields.containsKey("properties")) {
                        entry.properties = (Map<String, Object>) fields.get("properties");
                    }
                    if (fields.containsKey("labels")) {
                        entry.labels = new HashSet<String>((Collection<String>) fields.get("labels"));
                    }
                    if (fields.containsKey("parent")) {
                        Menu parent = namedMenus.get(fields.get("parent"));
                        if(parent == null) {
                            throw new RuntimeException("Navigation file references a non existing parent " + fields.get("parent"));
                        }
                        entry.parent = parent;
                        parent.children.add(entry);
                    }
                    namedMenus.put(name, entry);

                    // 保存entry到Menu
                    saveMenuToDB(applicationName, currentLoadVersion, entry);
                }
            }
            deleteUndefinedNavitation(applicationName, currentLoadVersion);
        }
    }


    public class NavigationYamlFile {
        public List<Menu> menus;
    }

    private static void saveMenuToDB(String applicationName, long currentLoadVersion, Menu entry) {
        SupplierNavigation nav = SupplierNavigation.find("byName", entry.name).first();
        if (nav == null) {
            nav = new SupplierNavigation();
            nav.name = entry.name;
            nav.createdAt = new Date();
        }
        nav.text = entry.text;
        nav.action = entry.action;
        nav.url = entry.url;
        // TODO: nav.parent
        nav.applicationName = applicationName;
        nav.loadVersion = currentLoadVersion;
        nav.updatedAt = new Date();

        nav.save();
    }

    private static void deleteUndefinedNavitation(String applicationName, long currentLoadVersion) {
        SupplierNavigation.deleteUndefinedNavigation(applicationName, currentLoadVersion);
    }


    public static ContextedMenu getMenu(String name) {
        if(!namedMenus.containsKey(name)) {
            throw new IllegalArgumentException("Menu '" + name + "' not defined.");
        }
        return new ContextedMenu(namedMenus.get(name), getMenuContext());
    }

    public static void clearMenuContext() {
        menuContext.set(null);
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
        MenuContext menuContext = new MenuContext(Request.current());
        return menuContext;
    }
}