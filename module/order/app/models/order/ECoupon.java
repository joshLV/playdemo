package models.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import models.accounts.Account;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.sales.Goods;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.db.jpa.Model;

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
    public BigDecimal eCouponPrice;
    
    @Column(name="income_price")
    public BigDecimal incomePrice;

    @Column(name="refund_price")
    public BigDecimal refundPrice;

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
	public static ECoupon query(String eCouponSn,Long companyId) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select distinct e from ECoupon e where 1=1 ") ;
		if (companyId != null) {
			sql.append(" and e.goods.companyId = :companyId");
			params.put("companyId", companyId);
		}
		if (StringUtils.isNotBlank(eCouponSn)) {
			sql.append(" and e.eCouponSn = :eCouponSn");
			params.put("eCouponSn", eCouponSn);
		}
		Query couponQery = entityManager.createQuery(sql.toString());
		for(Map.Entry<String,Object> entry : params.entrySet()){
			couponQery.setParameter(entry.getKey(), entry.getValue());
		}
		sql.append(" order by e.createdAt desc");

		List<ECoupon> queryList = couponQery.getResultList();
		if (queryList.size() ==0) {
			return null;
		}
		
		return queryList.get(0);
	}

	/**
	 * 根据页面录入券号查询对应信息
	 * 
	 * @param eCouponSn 券号
	 * @return
	 */
	public static Map<String,Object> queryInfo(String eCouponSn,Long companyId) {
		ECoupon eCoupon = query(eCouponSn,companyId);
		Map<String,Object> queryMap = new HashMap();
		if (eCoupon != null) {
			java.text.DateFormat df = new java.text.SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			queryMap.put("name", eCoupon.goods.name);
			queryMap.put("expireAt", eCoupon.goods.expireAt != null? df.format(eCoupon.goods.expireAt):null);
			queryMap.put("consumedAt",eCoupon.consumedAt != null ? df.format(eCoupon.consumedAt):null);
			queryMap.put("eCouponSn", eCoupon.eCouponSn);
			queryMap.put("refundAt", eCoupon.refundAt != null? df.format(eCoupon.refundAt):null);
			queryMap.put("status", eCoupon.status);
			queryMap.put("error",0);
		}

		return queryMap;
	}


	/**
	 * 修改券状态,并产生消费交易记录
	 * 
	 * @param eCouponSn
	 * @param companyId
	 */
	public static void update(String eCouponSn, Long companyId) {
		ECoupon eCoupon = query(eCouponSn,companyId);
		eCoupon.status= ECouponStatus.CONSUMED;
		eCoupon.save();
		
		//产生消费记录
		Account account = Account.findById(companyId);
		TradeBill tradeBill = TradeUtil.createConsumeTrade(eCouponSn, account, eCoupon.incomePrice,eCoupon.order.id);
		TradeUtil.success(tradeBill);
	}

    
}
