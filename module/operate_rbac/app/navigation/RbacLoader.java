package navigation;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import models.admin.OperateNavigation;
import models.admin.OperatePermission;
import models.admin.OperateRole;
import play.Logger;
import play.Play;
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

        loadRolesToDB(application.roles, loadVersion);

        loadPermissionsToDB(null /* parent permission */,
                application.permissions, applicationName, loadVersion);

        loadMenusToDB(null, application.menus, applicationName, loadVersion);

        // 清理更新前的数据
        deleteUndefinedMenus(applicationName, loadVersion);
        deleteUndefinedPermissions(applicationName, loadVersion);
    }

    /**
     * 删除之前版本的Permissions.
     * @param applicationName
     * @param loadVersion
     */
    private static void deleteUndefinedPermissions(String applicationName, long loadVersion) {
        OperatePermission.deleteUndefinedPermissions(applicationName, loadVersion);
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

    private static void savePermissionToDB(String applicationName, long loadVersion, Permission permission,
            Permission parentPermission) {
        OperatePermission operatePermission = OperatePermission.find("byApplicationNameAndKey", applicationName, permission.key).first();
        if (operatePermission == null) {
            operatePermission = new OperatePermission();
            operatePermission.key = permission.key;
            operatePermission.createdAt = new Date();
            operatePermission.displayOrder = 999;   // FIXME: 使用顺序值
        }
        operatePermission.text = permission.text;
        // TODO: nav.parent
        operatePermission.applicationName = applicationName;
        operatePermission.loadVersion = loadVersion;
        operatePermission.updatedAt = new Date();

        if (permission.getRoles() != null) {
            operatePermission.roles = new HashSet<>();
            for (String roleName : permission.getRoles()) {
                OperateRole role = OperateRole.find("byKey", roleName).first();
                if (role != null) {
                    operatePermission.roles.add(role);
                }
            }
        }

        if (parentPermission != null) {
            operatePermission.parent = OperatePermission.find("byApplicationNameAndKey", applicationName, parentPermission.key).first();
        }

        Logger.info("operatePermission.parent-" + operatePermission.parent);
        operatePermission.save();
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
        OperateRole operateRole = OperateRole.find("byKey", role.key).first();
        if (operateRole == null) {
            operateRole = new OperateRole();
            operateRole.key = role.key;
            operateRole.createdAt = new Date();
        }
        operateRole.text = role.text;
        operateRole.loadVersion = loadVersion;
        operateRole.updatedAt = new Date();

        operateRole.save();
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
        OperateNavigation operateNavigation = OperateNavigation.find("byName", menu.name).first();
        if (operateNavigation == null) {
            operateNavigation = new OperateNavigation();
            operateNavigation.name = menu.name;
            operateNavigation.createdAt = new Date();
        }
        operateNavigation.text = menu.text;
        operateNavigation.action = menu.action;
        operateNavigation.url = menu.url;
        operateNavigation.labels = menu.labelValue;
        operateNavigation.applicationName = applicationName;
        operateNavigation.loadVersion = currentLoadVersion;
        operateNavigation.updatedAt = new Date();

        if (Play.mode == Play.mode.DEV) {
            // FIXME: operateNavigation.devBaseUrl 应该使用localhost:9000这样的地址，现在这个不对
            operateNavigation.devBaseUrl = Play.configuration.getProperty("application.baseUrl");
        } else {
            operateNavigation.prodBaseUrl = Play.configuration.getProperty("application.baseUrl");
        }

        if (menu.getPermissions() != null) {
            operateNavigation.permissions = new HashSet<>();
            for (String permissionName : menu.getPermissions()) {
                OperatePermission permission = OperatePermission.find("byKey", permissionName).first();
                if (permission != null) {
                    operateNavigation.permissions.add(permission);
                }
            }
        }

        if (parentMenu != null) {
            operateNavigation.parent = OperateNavigation.find("byName", parentMenu.name).first();
        }

        operateNavigation.save();
    }

    private static void deleteUndefinedMenus(String applicationName, long currentLoadVersion) {
        OperateNavigation.deleteUndefinedNavigation(applicationName, currentLoadVersion);
    }


}