package helper;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.sales.PointGoods;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: hejun
 * Date: 12-8-3
 * Time: 下午4:12
 */

public class JifenTitle {
    /**
     * 返回首页的标题.
     *
     * @return
     */
    public static String getHomeTitle() {
        if (Play.runingInTestMode()) {
            return "首页";
        }
        return "优惠券网,代金券，优惠券,一百券网-网上消费券首选门户";
    }

    /**
     * 详细页标题
     *
     * @param goods
     * @return
     */
    public static String getDetailTitle(PointGoods goods) {
        String title = "【一百券】" + goods.name + "-优惠券,优惠券网,代金券" + (goods.keywords == null ? "" : ("【" + goods.keywords + "】"));
        return title;
    }

    /**
     * 详细页关键词
     *
     * @param goods
     * @return
     */
    public static String getDetailKeyWords(PointGoods goods) {
        final PointGoods final_goods = goods;
        Map<String, String> keywordsMap = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + goods.id,
                        "KEYWORDMAP"),
                new CacheCallBack<Map<String, String>>() {
                    @Override
                    public Map<String, String> loadData() {
                        PointGoods g = PointGoods.findById(final_goods.id);
                        return JifenTitle.generateKeywordsMap(g);
                    }
                });
        String keyWords = StringUtils.trimToEmpty(keywordsMap.get("goodsKeywords"));
        return "优惠券,优惠券网,代金券," + keyWords;
    }

    /**
     * 关键字
     *
     * @param goods
     * @return
     */
    public static Map<String, String> generateKeywordsMap(PointGoods goods) {
        Map<String, String> keywordsMap = new HashMap<>();

        List<String> categoryKeywords = new ArrayList<>();

        if (StringUtils.isNotBlank(goods.keywords)) {
            String[] ks = goods.keywords.split("[,;\\s]+");
            Collections.addAll(categoryKeywords, ks);
        }
        if (categoryKeywords.size() > 0) {
            keywordsMap.put("goodsKeywords",
                    StringUtils.join(categoryKeywords, ","));
        } else {
            keywordsMap.put("goodsKeywords", "");
        }
        return keywordsMap;
    }

}
