package models.order;

import models.sales.Goods;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="order_id",nullable=true)
    public Orders order;

    @ManyToOne
    Goods goods;

    @Required
    @Column(name="e_coupon_sn")
    public String eCouponSn;

    @Column(name="e_coupon_price")
    public Float eCouponPrice;

    @Column(name="refund_price")
    public Float refundPrice;

    @Column(name="created_at")
    @Temporal(TemporalType.DATE)
    public Date createdAt;

    @Column(name="consumed_at")
    public Date consumedAt;

    @Column(name="refund_at")
    public Date refundAt;
    
    @Column(name="buy_number")
    public int buyNumber;
    
    @Enumerated(EnumType.STRING)
    public ECouponStatus status;

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
