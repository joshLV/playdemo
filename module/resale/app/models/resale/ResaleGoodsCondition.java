package models.resale;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import models.sales.Brand;
import models.sales.GoodsStatus;

import org.apache.commons.lang.StringUtils;

import com.uhuila.common.constants.DeletedStatus;

/**
 * 商品查询条件.
 * <p/>
 * User: sujie
 * Date: 2/29/12
 * Time: 4:25 PM
 */
public class ResaleGoodsCondition {

	public long brandId = 0;
	public BigDecimal priceFrom = new BigDecimal(0);
	public BigDecimal priceTo = new BigDecimal(0);
	public String orderBy = getOrderBy(0);
	public String orderByType = "DESC";
	public int orderByNum = 0;
	public int orderByTypeNum = 0;

	private Map<String, Object> paramMap = new HashMap<>();

	public ResaleGoodsCondition() {
	}

	/**
	 * 拼接hql的查询条件.
	 *
	 * @param condStr hql的查询条件
	 */
	public ResaleGoodsCondition(String condStr) {
		String[] args = condStr.split("-");
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("ResaleGoodsCondition is illegal!");
		}
		if (args.length > 0) {
			brandId = StringUtils.isBlank(args[0]) ? 0 : Long
					.parseLong(args[0]);
		}
		if (args.length > 1) {
			priceFrom = StringUtils.isBlank(args[1]) ? new
					BigDecimal(0) : new BigDecimal(args[1]);
		}
		if (args.length > 2) {
			priceTo = StringUtils.isBlank(args[2]) ? new
					BigDecimal(0) : new BigDecimal(args[2]);
		}
		if (args.length > 3) {
			orderByNum = StringUtils.isBlank(args[3]) ? 0 : Integer.parseInt(args[3]);
			orderBy = StringUtils.isBlank(args[3]) ? getOrderBy(0)
					: getOrderBy(Integer.parseInt(args[3]));
		}
		if (args.length > 4) {
			orderByTypeNum = StringUtils.isBlank(args[4]) ? 1 : Integer.parseInt
					(args[4]);
			orderByType = "1".equals(args[4]) ? "DESC" : "ASC";
		}
	}


	public String getOrderByExpress() {
		String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
		return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}


	/**
	 * 排序条件
	 * 
	 * @param orderById
	 * @return
	 */
	private static String getOrderBy(int orderById) {
		String orderBy;
		switch (orderById) {
		case 1:
			orderBy = "g.saleCount";
			break;
		case 2:
			orderBy = "g.salePrice";
			break;
		case 3:
			orderBy = "g.discount";
			break;
		case 4:
			orderBy = "g.createdAt";
			break;
		default:
			orderBy = "g.createdAt";
			break;
		}
		return orderBy;
	}


	public String getPriceScopeExpress() {
		if (priceFrom.compareTo(BigDecimal.ZERO) == 0 && priceTo.compareTo(BigDecimal.ZERO) == 0) {
			return "全部";
		}
		if (priceTo.compareTo(BigDecimal.ZERO) == 0) {
			return priceFrom + "元以上";
		}
		return priceFrom + "-" + priceTo + "元";
	}

	/**
	 * 分销商查询条件
	 * 
	 * @return sql 查询条件
	 */
	public String getResaleFilter() {
		StringBuilder sql = new StringBuilder();
		sql.append(" g.deleted = :deleted");
		paramMap.put("deleted", DeletedStatus.UN_DELETED);
		if (brandId != 0) {
			sql.append(" and g.brand = :brand");
			Brand brand = new Brand();
			brand.id = brandId;
			paramMap.put("brand", brand);
		}

		if (priceFrom.compareTo(new BigDecimal(0)) > 0) {
			sql.append(" and g.salePrice >= :priceFrom");
			paramMap.put("priceFrom", priceFrom);
		}
		if (priceTo.compareTo(new BigDecimal(0)) > 0) {
			sql.append(" and g.salePrice <= :priceTo");
			paramMap.put("priceTo", priceTo);
		}
		return sql.toString();
	}
}
