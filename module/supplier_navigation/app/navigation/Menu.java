package navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import models.admin.SupplierNavigation;

import org.apache.commons.lang.StringUtils;

/**
 * Bare Menu
 *
 * Menu as they come from the navigation.yml file
 */
@XmlRootElement(name="navigation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Menu {
    
    @XmlTransient
    public Menu parent;
    
    @XmlAttribute(name="key")
    public String name;
    @XmlAttribute
    public String text;
    @XmlAttribute
    public String action;
    @XmlAttribute
    public String url;

    @XmlElement(name="navigation")
    public List<Menu> children = new ArrayList<Menu>();
    
    @XmlAttribute(name="labels")
    public String labelValue;
    
    @XmlTransient
    public Set<String> getLabels() {
        Set<String> labels = new HashSet<String>();
        
        if (!StringUtils.isEmpty(labelValue)) {
            String[] values = labelValue.split("[\\s,]+");
            for (String value : values) {
                labels.add(value);
            }
        }
        
        return labels;
    }
    
    @XmlTransient
    public String applicationName;
    
    @XmlTransient
    public Map<String, String> params = new HashMap<String, String>();
    
    @XmlTransient
    public Map<String, Object> properties = new HashMap<String, Object>();
    
    public boolean hasLink() {
        return url != null || action != null;
    }
    
    public String menuKey() {
        return applicationName + "." + name;
    }
    
     public static Menu from(SupplierNavigation navigation) {
         return from(navigation, true);
     }
    public static Menu from(SupplierNavigation navigation, boolean recure) {
        if (navigation == null) {
            return null;
        }
        Menu menu = new Menu();
        menu.text = navigation.text;
        menu.name = navigation.name;
        menu.action = navigation.action;
        menu.url = navigation.url;
        if (navigation.parent != null && recure) {
            menu.parent = from(navigation.parent, false);
        }
        menu.labelValue = navigation.labels;
        menu.applicationName = navigation.applicationName;
        
        if (navigation.children != null && recure) {
            menu.children = new ArrayList<Menu>();
            for (SupplierNavigation nav : navigation.children) {
                menu.children.add(from(nav, false)); 
            }
        }
        
        return menu;
    }
}