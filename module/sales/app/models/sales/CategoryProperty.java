package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 商品分类的属性.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 12:53 PM
 */
@Entity
@Table(name = "category_properties")
public class CategoryProperty extends Model {
    
    private static final long serialVersionUID = 7063981609113062L;
    
    /**
     * 属性名.
     */
    public String name;
    /**
     * 文本值.
     */
    @Enumerated(EnumType.STRING)
    public CategoryPropertyType type;
    /**
     * 图片
     */
    public String value;
    /**
     * 所属类目
     */
    @ManyToOne
    public Category category;
}
