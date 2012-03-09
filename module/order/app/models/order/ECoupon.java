package models.order;

import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pwg
 * Date: 12-3-5
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "e_coupon")
public class ECoupon extends Model {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static DecimalFormat decimalFormat = new DecimalFormat("00000");

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="order_id",nullable=true)
    public Orders order;

    @ManyToOne
    Goods goods;

    @Column(name="e_coupon_sn")
    public String eCouponSn;

    @Column(name="e_coupon_price")
    public BigDecimal eCouponPrice;

    @Column(name="refund_price")
    public BigDecimal refundPrice;

    @Column(name="created_at")
    @Temporal(TemporalType.DATE)
    public Date createdAt;

    @Column(name="consumed_at")
    public Date consumedAt;

    @Column(name="refund_at")
    public Date refundAt;
    
    @Enumerated(EnumType.STRING)
    public ECouponStatus status;

    public ECoupon(Orders order, Goods goods, BigDecimal eCouponPrice){
        this.order = order;
        this.goods = goods;
        this.eCouponPrice = eCouponPrice;
        this.refundPrice = eCouponPrice;
        this.createdAt = new Date();

        this.consumedAt = null;
        this.refundAt = null;
        this.status = ECouponStatus.UNCONSUMED;
        this.eCouponSn = generateSerialNumber();
    }

    private String generateSerialNumber() {
        int random = new Random().nextInt() % 10000;
        return dateFormat.format(new Date()) + decimalFormat.format(random);

    }

    /**
     * 根据页面录入券号查询对应信息
     * 
     * @param eCouponSn 券号
     * @return
     */
	public static Map<String,Object> query(String eCouponSn,Long companyId) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select distinct e from ECoupon e where 1=1 ") ;
		if (companyId != null) {
			sql.append(" and e.goods.companyId = :companyId");
			params.put("companyId", companyId);
		}
		Query couponQery = entityManager.createQuery(sql.toString());
		for(Map.Entry<String,Object> entry : params.entrySet()){
			couponQery.setParameter(entry.getKey(), entry.getValue());
		}
		sql.append(" order by e.createdAt desc");
		
		Map<String,Object> queryMap = new HashMap();
		List<ECoupon> queryList = couponQery.getResultList();
		for (ECoupon e:queryList){
			queryMap.put("name", e.goods.name);
			queryMap.put("expireAt", e.goods.expireAt);
			queryMap.put("status", e.status);
		}
		
		return queryMap;
	}
    
    
}
