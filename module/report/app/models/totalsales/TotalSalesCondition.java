package models.totalsales;

import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 销售汇总查询条件.
 *
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class TotalSalesCondition {

    public static final int BY_SUPPLIER = 0;
    public static final int BY_SHOP = 1;
    public static final int BY_GOODS = 2;
    public static final int BY_VERIFY_TYPE = 3;
    public static final String END_TIME = " 23:59";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public int type = BY_SUPPLIER;

    public String shopBeginHour;
    public String shopEndHour;

    /**
     * 商户.
     */
    public Long supplierId;

    /**
     * 商品.
     */
    public Long goodsId;

    /**
     * 门店.
     */
    public Long shopId;

    /**
     * 开始时间.
     */
    public Date beginAt = new Date();

    /**
     * 结束时间.
     */
    public Date endAt = DateUtil.getEndOfDay(new Date());


    public String orderBy = "e.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("e.status=:status"); //只统计已经消费的
        paramMap.put("status", ECouponStatus.CONSUMED);
        if (beginAt != null) {
            Date beginDate = Supplier.getShopHour(beginAt, shopEndHour, false);
            condBuilder.append(" and e.consumedAt >= :beginAt");
            paramMap.put("beginAt", beginAt);
        }
        if (endAt != null) {
            Date endDate = Supplier.getShopHour(endAt, shopEndHour, true);
            condBuilder.append(" and e.consumedAt <= :endAt");
            String dateStr = DateUtil.dateToString(endAt, 0) + (StringUtils.isBlank(shopEndHour) ? END_TIME : " " + shopEndHour);
            paramMap.put("endAt", DateUtil.stringToDate(dateStr, DATE_FORMAT));
        }

        if (supplierId != null && supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplier");
            paramMap.put("supplier", supplierId);
        }

        if (goodsId != null && goodsId != 0) {
            condBuilder.append(" and e.goods.id = :goodsId");
            paramMap.put("goodsId", goodsId);
        }

        if (shopId != null && shopId != 0) {
            condBuilder.append(" and e.shop.id = :shopId");
            paramMap.put("shopId", shopId);
        }

        Logger.info("TotalSalesCondition condition:" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "r.createdAt DESC" : orderBy + " " + orderType;
    }


    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getKeyColumn() {
        switch (type) {
            case BY_SUPPLIER:
                return "e.goods.supplierId";
            case BY_SHOP:
                return "e.shop.name";
            case BY_GOODS:
                return "e.goods.shortName";
            case BY_VERIFY_TYPE:
                return "e.verifyType";
        }
        return "e.goods.supplierId";
    }

    public String getKeyIdColumn() {
        switch (type) {
            case BY_SUPPLIER:
                return "e.goods.supplierId";
            case BY_SHOP:
                return "e.shop.id";
            case BY_GOODS:
                return "e.goods.id";
            case BY_VERIFY_TYPE:
                return "e.verifyType";
        }
        return "e.goods.supplierId";
    }

    public String getGroupBy() {
        switch (type) {
            case BY_SUPPLIER:
                return "e.goods.supplierId";
            case BY_SHOP:
                return "e.shop.name";
            case BY_GOODS:
                return "e.goods.shortName";
            case BY_VERIFY_TYPE:
                return "e.verifyType";
        }
        return "e.goods.supplierId";
    }


    public boolean needQueryTrends() {
        switch (type) {
            case BY_SUPPLIER:
                return supplierId != null && supplierId > 0l;
            case BY_SHOP:
                return shopId != null && shopId > 0l;
            case BY_GOODS:
                return goodsId != null && goodsId > 0l;
        }
        return false;
    }

    public boolean needQueryRatios() {
        switch (type) {
            case BY_SUPPLIER:
                return true;
            case BY_SHOP:
                return supplierId != null && supplierId > 0l;
            case BY_GOODS:
                return supplierId != null && supplierId > 0l;
        }
        return false;
    }
}
