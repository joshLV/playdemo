package models.yihaodian.shop;

import models.yihaodian.YHDParser;
import org.dom4j.Element;

/**
 * @author likang
 *         Date: 12-9-3
 */
public class UpdateResult {
    public Integer updateCount;

    public static YHDParser<UpdateResult> parser = new YHDParser<UpdateResult>() {
        @Override
        public UpdateResult parse(Element node) {
            UpdateResult result = new UpdateResult();
            result.updateCount = Integer.parseInt(node.getTextTrim());
            return result;
        }
    };
}
