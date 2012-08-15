package models.sales;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:23
 */
public class SecKillGoodsCondition {
    public String goodsTitle;
    public SecKillGoodsStatus status;
    private Map<String, Object> paramMap = new HashMap<>();

    public SecKillGoodsCondition() {

    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("1=1");
        if (StringUtils.isNotBlank(goodsTitle)) {
            condBuilder.append(" and g.goods.name like :goodsTitle");
            paramMap.put("goodsTitle", "%" + goodsTitle.trim() + "%");
        }
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


    public String getItemFilter(Long seckillId) {
        StringBuilder condBuilder = new StringBuilder();
        SecKillGoods goods = SecKillGoods.findById(seckillId);
        condBuilder.append("g.secKillGoods=:goods");
        paramMap.put("goods", goods);

        if (status != null) {
            condBuilder.append(" and g.status=:status");
            paramMap.put("status", status);
        }

        if (StringUtils.isNotBlank(goodsTitle)) {
            condBuilder.append(" and g.goodsTitle like :goodsTitle");
            paramMap.put("goodsTitle", "%" + goodsTitle.trim() + "%");
        }
        return condBuilder.toString();
    }
}
