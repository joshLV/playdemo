package models.sales;

import play.db.jpa.Model;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 商品分类的属性.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 12:53 PM
 */
@Entity
@Table(name = "category_properties")
@SolrSearchable
public class CategoryProperty extends Model {

    private static final long serialVersionUID = 7063981609113062L;

    /**
     * 属性名.
     */
    @SolrField
    public String name;
    /**
     * 文本值.
     */
    @Enumerated(EnumType.STRING)
    @SolrField
    public CategoryPropertyType type;
    /**
     * 图片
     */
    @SolrField
    public String value;
    /**
     * 所属类目
     */
    @ManyToOne
    public Category category;
}
