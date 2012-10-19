package models.yihaodian.shop;

import org.dom4j.Element;

/**
 * @author likang
 *         Date: 12-9-3
 */
public class UpdateResult {
    public Integer updateCount;

    public static Parser<UpdateResult> parser = new Parser<UpdateResult>() {
        @Override
        public UpdateResult parse(Element node) {
            UpdateResult result = new UpdateResult();
            result.updateCount = Integer.parseInt(node.getTextTrim());
            return result;
        }
    };
}
