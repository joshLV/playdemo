package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class IdNameResponse implements Serializable {
    private static final long serialVersionUID = 7063222063910330652L;

    public Long id;
    public String name;
}
