package navigation;

import models.admin.SupplierNavigation;
import models.admin.SupplierPermission;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.supplier.cas.CASUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    @XmlAttribute(name="display-order")
    public int displayOrder;

    @XmlElement(name="navigation")
    public List<Menu> children = new ArrayList<Menu>();
    
    @XmlAttribute(name="labels")
    public String labelValue;
    
    @XmlTransient
    public String devBaseUrl;
    
    @XmlTransient
    public String prodBaseUrl;
    
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
    
     
    @XmlAttribute(name="permissions")
    public String permissionsValue;
    
    @XmlTransient
    public Set<String> getPermissions() {
        Set<String> permissionSet = new HashSet<String>();
        
        if (!StringUtils.isEmpty(permissionsValue)) {
            String[] values = permissionsValue.split("[\\s,]+");
            for (String value : values) {
                permissionSet.add(value);
            }
        }
        
        return permissionSet;
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
    
    @XmlTransient
    public String getBaseUrl() {
        String subDomain = CASUtils.getSubDomain();
        String baseUrl = null;
        if (Play.mode == Play.Mode.DEV) {
            baseUrl = this.devBaseUrl;
        } else {
            baseUrl = this.prodBaseUrl;
        }
        return CASUtils.replaceSubDomain(baseUrl, subDomain);
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
        menu.devBaseUrl = navigation.devBaseUrl;
        menu.prodBaseUrl = navigation.prodBaseUrl;
        menu.url = navigation.url;
        if (navigation.parent != null && recure) {
            menu.parent = from(navigation.parent, false);
        }
        menu.labelValue = navigation.labels;
        menu.applicationName = navigation.applicationName;


        menu.permissionsValue = "";
        for (SupplierPermission perm : navigation.permissions) {
            menu.permissionsValue += perm.key + ",";
        }

        if (navigation.children != null && recure) {
            menu.children = new ArrayList<Menu>();
            for (SupplierNavigation nav : navigation.children) {
                menu.children.add(from(nav, false)); 
            }
        }
        
        return menu;
    }
}