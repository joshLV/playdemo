package models.yihaodian.response;

import models.yihaodian.YHDParser;
import org.dom4j.Element;
import play.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-26
 */
public class YHDMerchantCategory implements Serializable {
    private static final long serialVersionUID = 7063222863910330452L;

    public Long backOperatorId;
    public String code;
    public Boolean isLeaf = false;
    public String name;
    public Long parentId;
    public Long id;
    public String searchName;
    public Long forumId;
    public Boolean isVisible;
    public String picUrl;

    public List<YHDMerchantCategory> children;

    public static YHDParser<YHDMerchantCategory> parser = new YHDParser<YHDMerchantCategory>() {
        @Override
        public YHDMerchantCategory parse(Element node) {
            Logger.info(node.asXML());

            YHDMerchantCategory merchantCategory = new YHDMerchantCategory();

            merchantCategory.backOperatorId = Long.parseLong(node.elementTextTrim("backOperatorId"));
            merchantCategory.code = node.elementTextTrim("categoryCode");
            merchantCategory.isLeaf = "1".equals(node.elementTextTrim("categoryIsLeaf"));
            merchantCategory.name = node.elementTextTrim("categoryName");
            merchantCategory.parentId = Long.parseLong(node.elementTextTrim("categoryParentId"));
            merchantCategory.searchName = node.elementTextTrim("categorySearchName");
            merchantCategory.forumId = Long.parseLong(node.elementTextTrim("forumId"));
            merchantCategory.isVisible = "1".equals(node.elementTextTrim("isVisible"));
            merchantCategory.id = Long.parseLong(node.elementTextTrim("merchantCategoryId"));
            merchantCategory.picUrl = node.elementTextTrim("picUrl");

            return merchantCategory;
        }
    };
}
