package models.jingdong.groupbuy.request;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

/**
 * @author likang
 *         Date: 12-10-11
 */
public class QueryTeamSellCountRequest implements JDMessage {
    public Long venderTeamId;
    public Integer sellCount;
    @Override
    public boolean parse(Element root) {
        venderTeamId = Long.parseLong(root.elementTextTrim("VenderTeamId"));
        sellCount = Integer.parseInt(root.elementTextTrim("SellCount"));
        return true;
    }
}
