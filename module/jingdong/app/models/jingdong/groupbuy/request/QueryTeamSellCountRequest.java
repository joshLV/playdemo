package models.jingdong.groupbuy.request;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-10-11
 */
public class QueryTeamSellCountRequest implements JDMessage,Serializable {

    private static final long serialVersionUID = 7063246863910330652L;

    public Long venderTeamId;
    public Integer sellCount;
    @Override
    public boolean parse(Element root) {
        venderTeamId = Long.parseLong(root.elementTextTrim("VenderTeamId"));
        sellCount = Integer.parseInt(root.elementTextTrim("SellCount"));
        return true;
    }
}
