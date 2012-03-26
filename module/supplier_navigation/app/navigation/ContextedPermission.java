package navigation;

import java.util.HashSet;
import java.util.Set;

public class ContextedPermission {
    
    private static ThreadLocal<Set<String>> _allowPermissions = new ThreadLocal<>();

    public static void addAllowPermission(String permmission) {
        if (_allowPermissions.get() == null) {
            _allowPermissions.set(new HashSet<String>());
        }
        _allowPermissions.get().add(permmission);
    }
    
    public static void clean() {
        _allowPermissions.set(null);
    }
    

    public static Set<String> getAllowPermissions() {
        return _allowPermissions.get();	
    }    

    public static boolean hasPermission(String permission_key) {
        return _allowPermissions.get() != null && _allowPermissions.get().contains(permission_key);
    }
}
