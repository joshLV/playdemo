package operate.rbac;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="application")
@XmlAccessorType(XmlAccessType.FIELD)
public class Application implements Serializable {

    private static final long serialVersionUID = 9813912330652L;

    @XmlAttribute(name="text")
    public String text;

    @XmlElement(name="navigation")
    public List<Menu> menus = new ArrayList<Menu>();
    
    @XmlElement(name="permission")
    public List<Permission> permissions = new ArrayList<Permission>();
    
    @XmlElement(name="role")
    public List<Role> roles = new ArrayList<Role>();
    
    
}
