package models.order;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import com.uhuila.common.constants.DeletedStatus;

@Entity
@Table(name = "discount_codes")
public class DiscountCode extends Model {
    private static final long serialVersionUID = 2632320311992L;
    
    /**
     * 描述性的标题.
     * 会显示在订单页面上.
     */
    public String title;

	@Column(name="discount_sn", unique=true)
	public String discountSn;

	/**
	 * 对应折扣商品，如果有则只对单个商品有效.
	 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;
    
	/**
	 * 折扣总金额
	 */
	@Column(name="discount_amount")
	public BigDecimal discountAmount;

	/**
	 * 折扣比率.
	 */
	@Column(name="discount_percent")
	public BigDecimal discountPercent;

    /**
     * 秒杀开始时间
     */
    @Required
    @Column(name = "begin_at")
    @As(lang={"*"}, value={"yyyy-MM-dd HH:mm:ss"})
    public Date beginAt;

    /**
     * 秒杀结束时间
     */
    @Required
    @Column(name = "end_at")
    @As(lang={"*"}, value={"yyyy-MM-dd HH:mm:ss"})
    public Date endAt;

	@Column(name="created_at")
	public Date createdAt;
	
    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    public static void update(Long id, DiscountCode discountCodeForm) {
        DiscountCode discountCode = DiscountCode.findById(id);
        discountCode.title = discountCodeForm.title;
        discountCode.discountSn = discountCodeForm.discountSn;
        discountCode.goods = discountCodeForm.goods;
        discountCode.discountAmount = discountCodeForm.discountAmount;
        discountCode.discountPercent = discountCodeForm.discountPercent;
        discountCode.beginAt = discountCodeForm.beginAt;
        discountCode.endAt = discountCodeForm.endAt;
        discountCode.deleted = discountCodeForm.deleted;
        discountCode.save();
    }

    public static ModelPaginator getDiscountCodePage(int pageNumber,
            int pageSize, String discountSN) {
        ModelPaginator page;
        if (StringUtils.isNotEmpty(discountSN)) {
            page = new ModelPaginator(DiscountCode.class, "deleted = ? and discountSn like ?", DeletedStatus.UN_DELETED,
                    "%" + discountSN + "%").orderBy("id DESC");
        } else {
            page = new ModelPaginator(DiscountCode.class, "deleted = ? ", DeletedStatus.UN_DELETED).orderBy("id DESC");
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
