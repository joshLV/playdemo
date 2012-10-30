package models.yihaodian.response;

import models.yihaodian.YHDParser;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-10-26
 */
public class YHDProductCategory implements Serializable {
    private static final long serialVersionUID = 7061222013910330652L;

    public String code;
    public Boolean isLeaf;
    public Long id;
    public Long parentId;
    public String name;
    public String searchName;
    public Long forumId;
    public Boolean isVisible;
    public Integer listOrder;

    public static YHDParser<YHDProductCategory> parser = new YHDParser<YHDProductCategory>() {
        @Override
        public YHDProductCategory parse(Element node) {

            YHDProductCategory productCategory = new YHDProductCategory();
            productCategory.code = node.elementTextTrim("categoryCode");
            productCategory.id = Long.parseLong(node.elementTextTrim("categoryId"));
            productCategory.isLeaf = "1".equals(node.elementTextTrim("categoryIsLeaf"));
            productCategory.name = node.elementTextTrim("categoryName");
            productCategory.parentId = Long.parseLong(node.elementTextTrim("categoryParentId"));
            productCategory.searchName = node.elementTextTrim("categorySearchName");
            productCategory.forumId = Long.parseLong(node.elementTextTrim("forumId"));
            productCategory.isVisible = Boolean.parseBoolean(node.elementTextTrim("isVisible"));
            if(node.elementTextTrim("listOrder") != null){
                productCategory.listOrder = Integer.parseInt(node.elementTextTrim("listOrder"));
            }else {
                productCategory.listOrder = -1;
            }
            return productCategory;
        }
    };
}
