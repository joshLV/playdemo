package navigation;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import models.admin.SupplierNavigation;
import models.admin.SupplierPermission;
import models.admin.SupplierRole;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.mvc.Router;
import play.vfs.VirtualFile;

/**
 * Keeper of the bare RBAC all defines.
 * 
 * This class holds a static reference to all the Menus. You can retrive a Menu from this class,
 * which will be automatically wrapped in a ContextedMenu, ready to be inserted into your view.
 */
public class RbacLoader {

    public static void init(VirtualFile file)  {
        JAXBContext jaxbContext;
        Logger.info(RbacLoader.class.getName() + " init.");
        try {
            Class[] clazzes = new Class[] {
                    Application.class,
                    Menu.class,
                    Permission.class,
                    Role.class
            };
            jaxbContext = JAXBContext.newInstance(clazzes);
                Unmarshaller um = jaxbContext.createUnmarshaller();
                Application application = (Application) um.unmarshal(file.getRealFile());
                init(application);
        } catch (Exception e) {
            Logger.error(e, "Problem initializing context: %s", e.getMessage());
        }
        
    }

    public static void init(Application application) {
        long loadVersion = System.currentTimeMillis();
        String applicationName = Play.configuration.getProperty("application.name");
        loadMenusToDB(null, application.menus, applicationName, loadVersion);
        deleteUndefinedMenus(applicationName, loadVersion);    
        
        loadRolesToDB(application.roles, loadVersion);
        deleteUndefinedRoles(loadVersion);    
        
        loadPermissionsToDB(null, application.permissions, applicationName, loadVersion);
        deleteUndefinedPermissions(applicationName, loadVersion);    
    }
    
    /**
     * 删除之前版本的Permissions.
     * @param applicationName
     * @param loadVersion
     */
    private static void deleteUndefinedPermissions(String applicationName, long loadVersion) {
        SupplierPermission.deleteUndefinedPermissions(applicationName, loadVersion);
    }

    /**
     * 加载权限到数据库.
     * @param object
     * @param menus
     * @param applicationName
     * @param loadVersion
     */
    private static void loadPermissionsToDB(Permission parentPermission, List<Permission> permissions, String applicationName, long loadVersion) {
        for (Permission permission : permissions) {
            permission.parent = parentPermission;
            savePermissionToDB(applicationName, loadVersion, permission, parentPermission);

            if (!permission.children.isEmpty()) {
                loadPermissionsToDB(permission, permission.children, applicationName, loadVersion);
            }
        }
    }

    private static void savePermissionToDB(String applicationName, long loadVersion, Permission menu,
            Permission parentPermission) {
        SupplierPermission supplierPermission = SupplierPermission.find("byKey", menu.key).first();
        if (supplierPermission == null) {
            supplierPermission = new SupplierPermission();
            supplierPermission.key = menu.key;
            supplierPermission.createdAt = new Date();
        }
        supplierPermission.text = menu.text;
        // TODO: nav.parent
        supplierPermission.applicationName = applicationName;
        supplierPermission.loadVersion = loadVersion;
        supplierPermission.updatedAt = new Date();

        if (parentPermission != null) {
            supplierPermission.parent = SupplierPermission.find("byKey", parentPermission.key).first();
        }

        supplierPermission.save();
    }

    /**
     * 删除之前版本的Roles.
     * @param applicationName
     * @param loadVersion
     */
    private static void deleteUndefinedRoles(long loadVersion) {
        SupplierRole.deleteUndefinedRoles(loadVersion);
    }

    /**
     * 加载Roles到数据库.
     * @param roles
     * @param applicationName
     * @param loadVersion
     */
    private static void loadRolesToDB(List<Role> roles, long loadVersion) {
        for (Role role : roles) {
            saveRoleToDB(loadVersion, role);
        }        
    }

    private static void saveRoleToDB(long loadVersion, Role role) {
        SupplierRole supplierRole = SupplierRole.find("byKey", role.key).first();
        if (supplierRole == null) {
            supplierRole = new SupplierRole();
            supplierRole.key = role.key;
            supplierRole.createdAt = new Date();
        }
        supplierRole.text = role.text;
        supplierRole.loadVersion = loadVersion;
        supplierRole.updatedAt = new Date();

        supplierRole.save();
    }

    public static void loadMenusToDB(Menu parentMenu, List<Menu> menus, String applicationName, long loadVersion) {
        for (Menu menu : menus) {
            menu.parent = parentMenu;
            saveMenuToDB(applicationName, loadVersion, menu, parentMenu);
            if (!menu.children.isEmpty()) {
                loadMenusToDB(menu, menu.children, applicationName, loadVersion);
            }
        }
    }

    private static void saveMenuToDB(String applicationName, long currentLoadVersion, Menu menu, Menu parentMenu) {
        SupplierNavigation supplierNavigation = SupplierNavigation.find("byName", menu.name).first();
        if (supplierNavigation == null) {
            supplierNavigation = new SupplierNavigation();
            supplierNavigation.name = menu.name;
            supplierNavigation.createdAt = new Date();
        }
        supplierNavigation.text = menu.text;
        supplierNavigation.action = menu.action;
        supplierNavigation.url = menu.url;
        supplierNavigation.labels = menu.labelValue;
        // TODO: nav.parent
        supplierNavigation.applicationName = applicationName;
        supplierNavigation.loadVersion = currentLoadVersion;
        supplierNavigation.updatedAt = new Date();

        if (parentMenu != null) {
            supplierNavigation.parent = SupplierNavigation.find("byName", parentMenu.name).first();
        }

        supplierNavigation.save();
    }

    private static void deleteUndefinedMenus(String applicationName, long currentLoadVersion) {
        SupplierNavigation.deleteUndefinedNavigation(applicationName, currentLoadVersion);
    }


}