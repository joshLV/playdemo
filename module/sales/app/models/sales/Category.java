package models.sales;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 12:53 PM
 */
@Entity
@Table(name = "categories")
public class Category extends Model {
    /**
     * 商品标识.
     */
    public long goodsId;
    /**
     * 类目标识.
     */
    public long categoryId;
}
