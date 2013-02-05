package models.yihaodian;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;


/**
 * @author likang
 *         Date: 12-8-30
 */
public class YHDResponse {
    public int errorCount = 1;
    public List<Node> errors = new ArrayList<>(); // errorCode errorDes pkInfo

    public Node data;

    public boolean isOk() {
        return errorCount == 0;
    }
}
