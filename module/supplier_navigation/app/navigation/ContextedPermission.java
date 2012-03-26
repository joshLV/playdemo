package navigation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import models.admin.SupplierPermission;
import models.admin.SupplierUser;

public class ContextedPermission {
    
    private static ThreadLocal<Set<String>> _allowPermissions = new ThreadLocal<>();

    public static void addAllowPermission(String permmission) {
        if (_allowPermissions.get() == null) {
            _allowPermissions.set(new HashSet<String>());
        }
        _allowPermissions.get().add(permmission);
    }
    
    public static void clean() {
        if (_allowPermissions.get() != null) {
            _allowPermissions.get().clear();
        }
        _allowPermissions.set(null);
    }
    

    public static Set<String> getAllowPermissions() {
        return _allowPermissions.get();	
    }    

    public static boolean hasPermission(String permission_key) {
        return _allowPermissions.get() != null && _allowPermissions.get().contains(permission_key);
    }
    
    /**
     * 初始化用户的权限数据.
     * @param userName
     */
    public static void init(String userName) {
        // 查出当前用户的所有权限
        SupplierUser user = SupplierUser.find("byLoginName", userName).first();
        
        if (user == null) {
            return;
        }
        
        for (SupplierPermission perm : user.permissions) {
            addAllowPermission(perm.key);
        }
        
        // 查出当前用户从角色继承的所有权限
        List<SupplierPermission> rolePerms = SupplierPermission.findByUserRole(user.id);
        for (SupplierPermission perm : rolePerms) {
            addAllowPermission(perm.key);
        }
        
    }

}
