package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class UploadTeamResponse implements JDMessage{
    public Long jdTeamId;


    @Override
    public boolean parse(Element root) {
        jdTeamId = Long.parseLong(root.elementTextTrim("JdTeamId"));
        return true;
    }
}
