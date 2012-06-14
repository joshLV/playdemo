package models.cms;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 */
@Entity
@Table(name = "promotion")
public class Promotion extends Model {
    @Required
    public String name;         //活动名

    public String description;  //活动描述

    @Required
    public BigDecimal budget;   //活动预算

    public BigDecimal expenses; //已花费

    @Required
    public Date effectAt;       //起始时间

    @Required
    public Date expiredAt;      //截止日期

    @Column(name = "created_at")
    public Date createdAt;

    public Promotion(){
        this.budget = BigDecimal.ZERO;
        this.expenses = BigDecimal.ZERO;
        this.createdAt = new Date();
    }


    public static JPAExtPaginator<Promotion> findByCondition(
            PromotionCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Promotion> page = new JPAExtPaginator<>(
                null, null, Promotion.class, condition.getFilter(), condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
