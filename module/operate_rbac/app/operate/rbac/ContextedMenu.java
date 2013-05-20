package operate.rbac;

import org.apache.commons.collections.CollectionUtils;
import play.Play;
import play.mvc.Router;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wrapper for Menu and MenuContext
 *
 * This class wraps a bare Menu and a MenuContext and has methods that are useful in your menu renderer
 */
public class ContextedMenu implements Serializable {

    private static final long serialVersionUID = 70901122330652L;

    public Menu menu;
    public MenuContext menuContext;

    private List<ContextedMenu> children;
    private List<ContextedMenu> visibleChildren;
    private Boolean hasActiveDescendant = null;

    public ContextedMenu(Menu menu, MenuContext menuContext) {
        if(menu == null) throw new NullPointerException("menu null");
        if(menuContext == null) throw new NullPointerException("menuContext null");
        this.menu = menu;
        this.menuContext = menuContext;
    }

    public boolean isActive() {
        return menu.applicationName.equals(Play.configuration.get("application.name"))
                && ((menu.action != null && menuContext.hasActiveAction(menu.action))
                      || (menuContext.hasActiveName(menu.name))
                      );
    }

    public boolean hasActiveDescendant() {
        if(hasActiveDescendant == null) {
            if(isActive()) {
                hasActiveDescendant = true;
            } else {
                for(ContextedMenu visibleChild : getVisibleChildren()) {
                    if(visibleChild.hasActiveDescendant()) {
                        hasActiveDescendant = true;
                    }
                }
                if(hasActiveDescendant == null) {
                    hasActiveDescendant = false;
                }
            }
        }
        return hasActiveDescendant;
    }

    public boolean isVisible() {
        if(menu.getLabels().isEmpty()) {
            return true;
        } else {
            return CollectionUtils.containsAny(menu.getLabels(), menuContext.activeLabels);
        }
    }

    public void setChildren(List<ContextedMenu> children) {
        this.children = children;
    }

    public List<ContextedMenu> getChildren() {
        if(children == null) {
            children = new ArrayList<>(menu.children.size());
            for(Menu childMenu : menu.children) {
                if (childMenu.getPermissions() == null ||
                        childMenu.getPermissions().size() == 0 ||
                        ContextedPermission.hasPermissionKeys(childMenu.getPermissions())) {
                    children.add(new ContextedMenu(childMenu, menuContext));
                }
            }
        }
        return children;
    }

    public List<ContextedMenu> getVisibleChildren() {
        if(visibleChildren == null) {
            visibleChildren = new ArrayList<ContextedMenu>();
            for(ContextedMenu menu : getChildren()) {
                if(menu.isVisible()) {
                    visibleChildren.add(menu);
                }
            }
        }
        return visibleChildren;
    }

    public String getLink() {
        if(menu.url != null) {
            return menu.url;
        }
        if(menu.action == null) {
            return null;
        } else {
            return Router.reverse(menu.action, getSubstitutedParams()).url;
        }
    }
    
    public String getBaseUrl() {
        return menu.getBaseUrl();
    }

    public String getText() {
        return menu.text;
    }

    public boolean hasLink() {
        return menu.hasLink();
    }
    
    public Object getProperty(String propertyName) {
        return menu.properties.get(propertyName);
    }

    protected Map<String, Object> getSubstitutedParams() {
        Map<String, Object> substitutedParams = new HashMap<String, Object>();
        for(Entry<String, String> param : menu.params.entrySet()) {
            String value = param.getValue();
            for(Entry<String, Object> substitution : menuContext.substitutions.entrySet()) {
                value = value.replace(":" + substitution.getKey(), substitution.getValue().toString());
            }
            substitutedParams.put(param.getKey(), value);
        }
        return substitutedParams;
    }

    @Override
    public String toString() {
        // TODO: Improve
        return "ContextedMenu " + menu.text;
    }
}
