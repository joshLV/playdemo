package navigation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import play.mvc.Http.Request;

/**
 * Provide context for a Menu
 *
 * a MenuContext holds a list of active actions, active labels and substitutions, so that the renderer
 * knows which Menu is active and which should be visible.
 */
public class MenuContext {

    public Set<String> activeActions = new HashSet<String>();
    public Set<String> activeLabels = new HashSet<String>();
    public Set<String> activeNames = new HashSet<String>();
    public Map<String, Object> substitutions = new HashMap<String, Object>();

    public MenuContext(Request request, Set<String> _activeNames) {
        addActiveAction(request.action);
        if (_activeNames != null) {
            activeNames.addAll(_activeNames);
        }
    }

    public void addActiveAction(String action) {
        activeActions.add(action);
    }

    public void setActiveAction(String action) {
        activeActions.clear();
        addActiveAction(action);
    }

    public void setActiveActions(Collection<String> actions) {
        activeActions.clear();
        activeActions.addAll(actions);
    }

    public boolean hasActiveAction(String action) {
        return activeActions.contains(action);
    }
    
    public boolean hasActiveName(String name) {
        return activeNames.contains(name);
    }

    public void addActiveLabel(String label) {
        activeLabels.add(label);
    }

    public void setActiveLabel(String label) {
        activeLabels.clear();
        addActiveLabel(label);
    }
    
    
    public void addActiveNames(String name) {
        activeNames.add(name);
    }

    public void setActiveLabels(Collection<String> labels) {
        activeLabels.clear();
        activeLabels.addAll(labels);
    }

}