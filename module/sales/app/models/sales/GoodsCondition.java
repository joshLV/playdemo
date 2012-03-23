package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品查询条件.
 * <p/>
 * User: sujie
 * Date: 2/29/12
 * Time: 4:25 PM
 */
public class GoodsCondition {
	public static final String SHANGHAI = "021";

	public long supplierId = 0;
	public long categoryId = 0;
	public String cityId = SHANGHAI;
	public String districtId = "0";
	public String areaId = "0";
	public long brandId = 0;
	public BigDecimal priceFrom = new BigDecimal(0);
	public BigDecimal priceTo = new BigDecimal(0);
	public String orderBy = getOrderBy(0);
	public String orderByType = "DESC";
	public int orderByNum = 0;
	public int orderByTypeNum = 0;
	public String name;
	public String no;
	public BigDecimal salePriceBegin;
	public BigDecimal salePriceEnd;
	public Integer saleCountBegin;
	public Integer saleCountEnd;
	public GoodsStatus status;

	private Map<String, Object> paramMap = new HashMap<>();

	public GoodsCondition() {

	}

	/**
	 * 拼接hql的查询条件.
	 *
	 * @param condStr hql的查询条件
	 */
	public GoodsCondition(String condStr) {
		String[] args = condStr.split("-");
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("GoodsCondition is illegal!");
		}

		if (args.length > 0) {
			categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
					.parseLong(args[0]);
		}
		if (args.length > 1) {
			cityId = StringUtils.isBlank(args[1]) ? SHANGHAI : args[1];
		}
		if (args.length > 2) {
			districtId = args[2];
		}
		if (args.length > 3) {
			areaId = args[3];
		}
		if (args.length > 4) {
			if (isValidAreaId(districtId) && !areaId.contains(districtId)) {
				areaId = "0";
			}
			brandId = StringUtils.isBlank(args[4]) ? 0 : Long
					.parseLong(args[4]);
		}
		if (args.length > 5) {
			priceFrom = StringUtils.isBlank(args[5]) ? new
					BigDecimal(0) : new BigDecimal(args[5]);
		}
		if (args.length > 6) {
			priceTo = StringUtils.isBlank(args[6]) ? new
					BigDecimal(0) : new BigDecimal(args[6]);
		}
		if (args.length > 7) {
			orderByNum = StringUtils.isBlank(args[7]) ? 0 : Integer.parseInt(args[7]);
			orderBy = StringUtils.isBlank(args[7]) ? getOrderBy(0)
					: getOrderBy(Integer.parseInt(args[7]));
		}
		if (args.length > 8) {
			orderByTypeNum = StringUtils.isBlank(args[8]) ? 1 : Integer.parseInt
					(args[8]);
			orderByType = "1".equals(args[8]) ? "DESC" : "ASC";
		}
	}

	public String getFilter() {
		StringBuilder condBuilder = new StringBuilder();
		condBuilder.append(" g.deleted = :deleted and g.status != :notMatchStatus");
		paramMap.put("deleted", DeletedStatus.UN_DELETED);
		paramMap.put("notMatchStatus", GoodsStatus.UNCREATED);

		if (isValidAreaId(areaId)) {
			condBuilder.append(" and g.id in (select g.id from g.shops s " +
					"where s.areaId = :areaId)");
			paramMap.put("areaId", areaId);
		} else if (isValidAreaId(districtId)) {
			condBuilder.append(" and g.id in (select g.id from g.shops s " +
					"where s.areaId like :areaId)");
			paramMap.put("areaId", districtId + "%");
		} else if (isValidAreaId(cityId)) {
			condBuilder.append(" and g.id in (select g.id from g.shops s " +
					"where s.areaId like :areaId)");
			paramMap.put("areaId", cityId + "%");
		}
		if (supplierId != 0) {
			condBuilder.append(" and g.supplierId = :supplierId)");
			paramMap.put("supplierId", supplierId);
		}
		if (categoryId != 0) {
			condBuilder.append(" and g.id in (select g.id from " +
					"g.categories c where c.id = :categoryId)");
			paramMap.put("categoryId", categoryId);
		}
		if (brandId != 0) {
			condBuilder.append(" and g.brand = :brand");
			Brand brand = new Brand();
			brand.id = brandId;
			paramMap.put("brand", brand);
		}
		if (priceFrom.compareTo(new BigDecimal(0)) > 0) {
			condBuilder.append(" and g.salePrice >= :priceFrom");
			paramMap.put("priceFrom", priceFrom);
		}
		if (priceTo.compareTo(new BigDecimal(0)) > 0) {
			condBuilder.append(" and g.salePrice <= :priceTo");
			paramMap.put("priceTo", priceTo);
		}

		if (StringUtils.isNotBlank(name)) {
			condBuilder.append(" and name like :name");
			paramMap.put("name", "%" + name.trim() + "%");
		}

		if (StringUtils.isNotBlank(no)) {
			condBuilder.append(" and no like :no ");
			paramMap.put("no", "%" + no.trim() + "%");
		}

		if (status != null) {
			condBuilder.append(" and status = :status ");
			paramMap.put("status", status);
		}

		if (salePriceBegin != null) {
			condBuilder.append(" and salePrice >= :salePriceBegin");
			paramMap.put("salePriceBegin", salePriceBegin);
		}

		if (salePriceEnd != null) {
			condBuilder.append(" and salePrice <= :salePriceEnd");
			paramMap.put("salePriceEnd", salePriceEnd);
		}

		if (saleCountBegin != null && saleCountBegin >= 0) {
			condBuilder.append(" and saleCount >= :saleCountBegin");
			paramMap.put("saleCountBegin", saleCountBegin);
		}

		if (saleCountEnd != null && saleCountEnd >= 0) {
			condBuilder.append(" and saleCount <= :saleCountEnd");
			paramMap.put("saleCountEnd", saleCountEnd);
		}
		return condBuilder.toString();
	}

	public String getOrderByExpress() {
		String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
		return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}


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


	public static boolean isValidAreaId(String id) {
		if (StringUtils.contains(id, ' ')) {
			return false;
		}
		if ("0".equals(id)) {
			return false;
		}
		return true;
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

	public String getUrl() {
		return "/goods/list/" + categoryId + '-' + cityId + '-' + districtId + '-' + areaId + '-' + brandId + '-' + priceFrom + '-' + priceTo
				+ '-' + orderByNum + '-' + orderByTypeNum;
	}

	public boolean isDefault() {
		return categoryId == 0 && SHANGHAI.equals(cityId) && !isValidAreaId(districtId) && !isValidAreaId(areaId) &&
				brandId == 0 && priceFrom.compareTo(BigDecimal.ZERO) == 0 && priceTo.compareTo(BigDecimal.ZERO) == 0;
	}

	public String getResaleFilter() {
		StringBuilder sql = new StringBuilder();
		sql.append(" g.deleted = :deleted");
		paramMap.put("deleted", DeletedStatus.UN_DELETED);
		System.out.println("AAAAAAAAAAAAAAAAAAA"+brandId);
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
System.out.println("AAAAAAAAAAAAAAAAAAA"+sql.toString());
		return sql.toString();
	}
	
	/**
	 * 拼接hql的查询条件.
	 *
	 * @param condStr hql的查询条件
	 * @return 
	 */
	public GoodsCondition con(String condStr) {
		String[] args = condStr.split("-");
		
		System.out.println("bbbbbbbbbbb"+args.length);
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("GoodsCondition is illegal!");
		}

		if (args.length > 0) {
			categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
					.parseLong(args[0]);
		}
		if (args.length > 1) {
			cityId = StringUtils.isBlank(args[1]) ? SHANGHAI : args[1];
		}
		if (args.length > 2) {
			districtId = args[2];
		}
		if (args.length > 3) {
			areaId = args[3];
		}
		if (args.length > 4) {
			if (isValidAreaId(districtId) && !areaId.contains(districtId)) {
				areaId = "0";
			}
			brandId = StringUtils.isBlank(args[4]) ? 0 : Long
					.parseLong(args[4]);
		}
		if (args.length > 5) {
			priceFrom = StringUtils.isBlank(args[5]) ? new
					BigDecimal(0) : new BigDecimal(args[5]);
		}
		if (args.length > 6) {
			priceTo = StringUtils.isBlank(args[6]) ? new
					BigDecimal(0) : new BigDecimal(args[6]);
		}
		if (args.length > 7) {
			orderByNum = StringUtils.isBlank(args[7]) ? 0 : Integer.parseInt(args[7]);
			orderBy = StringUtils.isBlank(args[7]) ? getOrderBy(0)
					: getOrderBy(Integer.parseInt(args[7]));
		}
		if (args.length > 8) {
			orderByTypeNum = StringUtils.isBlank(args[8]) ? 1 : Integer.parseInt
					(args[8]);
			orderByType = "1".equals(args[8]) ? "DESC" : "ASC";
		}
		return null;
	}
}
