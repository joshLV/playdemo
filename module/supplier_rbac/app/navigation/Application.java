package navigation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="application")
@XmlAccessorType(XmlAccessType.FIELD)
public class Application {
        
    @XmlAttribute(name="text")
    public String text;

    @XmlElement(name="navigation")
    public List<Menu> menus = new ArrayList<Menu>();
    
    @XmlElement(name="permission")
    public List<Permission> permissions = new ArrayList<Permission>();
    
    @XmlElement(name="role")
    public List<Role> roles = new ArrayList<Role>();
    
    
}
