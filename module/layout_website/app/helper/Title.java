package helper;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsHistory;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Title {

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
    public static String getDetailTitle(Goods goods) {
        String title = "【" + (goods.brand == null ? "一百券" : goods.brand.name)
                + "】" + goods.name + "-优惠券,优惠券网,代金券" + (goods.keywords == null ? "" : ("【" + goods.keywords + "】"));
        return title;
    }

    public static String getDetailTitle(GoodsHistory goodsHistory) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsHistory.goodsId);
        return getDetailTitle(goods);
    }

    /**
     * 详细页关键词
     *
     * @param goods
     * @return
     */
    public static String getDetailKeyWords(Goods goods) {
        final Goods final_goods = goods;
        Map<String, String> keywordsMap = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + goods.id,
                        "KEYWORDMAP"),
                new CacheCallBack<Map<String, String>>() {
                    @Override
                    public Map<String, String> loadData() {
                        Goods g = Goods.findById(final_goods.id);
                        return Title.generateKeywordsMap(g);
                    }
                });
        String keyWords = StringUtils.trimToEmpty(keywordsMap.get("goodsKeywords"));
        return "优惠券,优惠券网,代金券," + keyWords;
    }

    public static String getDetailKeyWords(GoodsHistory goodsHistory) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsHistory.goodsId);
        return getDetailKeyWords(goods);
    }


    /**
     * 关键字
     *
     * @param goods
     * @return
     */
    public static Map<String, String> generateKeywordsMap(Goods goods) {
        Map<String, String> keywordsMap = new HashMap<>();

        List<String> categoryKeywords = new ArrayList<>();
        if (goods.categories != null) {
            for (Category c : goods.categories) {
                if (c.parentCategory != null) {
                    keywordsMap.put("categoryId",
                            String.valueOf(c.parentCategory.id));
                } else {
                    keywordsMap.put("categoryId", String.valueOf(c.id));
                }
                if (StringUtils.isNotBlank(c.keywords)) {
                    String[] ks = c.keywords.split("[,;\\s]+");
                    Collections.addAll(categoryKeywords, ks);
                }
            }
        }
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

    public static String copyRightTitle() {
        return "©2012-2013 一百券 yibaiquan.com 版权所有 <span> 沪ICP备08114451号</span>";
    }
}
