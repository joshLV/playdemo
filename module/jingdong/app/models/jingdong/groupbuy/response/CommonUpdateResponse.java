package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-12-28
 */
public class CommonUpdateResponse implements JDMessage, Serializable {
    private static final long serialVersionUID = 1069218043040230602L;
    public Long jdTeamId;
    public Long venderTeamId;

    @Override
    public boolean parse(Element root) {
        jdTeamId = Long.parseLong(root.elementTextTrim("JdTeamId"));
        venderTeamId = Long.parseLong(root.elementTextTrim("VenderTeamId"));
        return true;
    }
}
