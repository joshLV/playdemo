package navigation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name = "permission")
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission {

    @XmlTransient
    public Permission parent;

    @XmlAttribute
    public String key;

    @XmlAttribute
    public String text;

    @XmlElement(name = "permission")
    public List<Permission> children = new ArrayList<Permission>();

    @XmlAttribute(name = "roles")
    public String rolesValue;

    @XmlTransient
    public Set<String> getRoles() {
        Set<String> roles = new HashSet<String>();

        if (!StringUtils.isEmpty(rolesValue)) {
            String[] values = rolesValue.split("[\\s,]+");
            for (String value : values) {
                roles.add(value);
            }
        }

        return roles;
    }

}