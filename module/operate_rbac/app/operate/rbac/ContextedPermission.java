package operate.rbac;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.operator.OperatePermission;
import models.operator.OperateUser;
import play.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContextedPermission implements Serializable {

    private static final long serialVersionUID = 7009163912330652L;

    private static ThreadLocal<Set<String>> _allowPermissions = new ThreadLocal<>();

    public static void clean() {
        if (_allowPermissions.get() != null) {
            _allowPermissions.get().clear();
        }
        _allowPermissions.set(null);
    }


    public static Set<String> getAllowPermissions() {
        return _allowPermissions.get();
    }

    /**
     * 初始化用户的权限数据.
     *
     * @param user
     */
    public static void init(final OperateUser user) {
        if (user == null) {
            return;
        }

        Set<String> tmpPermKeySet = CacheHelper.getCache(
                CacheHelper.getCacheKey(
                        new String[]{OperateUser.CACHEKEY, OperateUser.CACHEKEY + user.id},
                        "PERMKEYS"),
                new CacheCallBack<Set<String>>() {
                    @Override
                    public Set<String> loadData() {
                        Set<String> permKeySet = new HashSet<>();

                        Logger.debug("user: %s, permissions: %d", user.loginName, user.permissions.size());
                        for (OperatePermission perm : user.permissions) {
                            permKeySet.add(perm.key);
                        }

                        // 查出当前用户从角色继承的所有权限
                        List<OperatePermission> rolePerms = OperatePermission.findByUserRole(user.id);
                        for (OperatePermission perm : rolePerms) {
                            Logger.debug("user: %s rold: %s", user.loginName, perm.key);
                            permKeySet.add(perm.key);
                        }
                        return permKeySet;
                    }
                });

        _allowPermissions.set(tmpPermKeySet);
    }

    public static boolean hasPermission(String permission_key) {
        return _allowPermissions.get() != null && _allowPermissions.get().contains(permission_key);
    }

    public static boolean hasPermissions(Set<OperatePermission> permissions) {
        for (OperatePermission perm : permissions) {
            if (hasPermission(perm.key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPermissionKeys(Set<String> permissions) {
        for (String perm : permissions) {
            if (hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

}
