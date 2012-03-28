package operate.rbac;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="role")
@XmlAccessorType(XmlAccessType.FIELD)
public class Role {

   @XmlAttribute
   public String key;
   
   @XmlAttribute
   public String text;
   
}
