package operate.rbac;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.FIELD)
public class Role implements Serializable {

    private static final long serialVersionUID = 956063912330652L;

    @XmlAttribute
    public String key;

    @XmlAttribute
    public String text;

}
