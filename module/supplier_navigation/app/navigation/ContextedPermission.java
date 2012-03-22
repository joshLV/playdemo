package navigation;

import java.util.HashSet;
import java.util.Set;

public class ContextedPermission {
    
    private Set<String> allowPermissions;

    public void addAllowPermission(String permmission) {
        if (allowPermissions == null) {
            allowPermissions = new HashSet<String>();
        }
        allowPermissions.add(permmission);
    }
}
