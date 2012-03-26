package navigation;

import java.util.List;
import models.admin.SupplierPermission;
import models.admin.SupplierUser;

public class PermissionHandler {

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
            ContextedPermission.addAllowPermission(perm.key);
        }
        
        // 查出当前用户从角色继承的所有权限
        List<SupplierPermission> rolePerms = SupplierPermission.findByUserRole(user.id);
        for (SupplierPermission perm : rolePerms) {
            ContextedPermission.addAllowPermission(perm.key);
        }
        
    }
    
    public static void clean() {
        ContextedPermission.clean();   
    }



    public static boolean hasPermission(String permission_key) {
        return ContextedPermission.hasPermission(permission_key);
    }

}
