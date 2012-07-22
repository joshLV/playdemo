package Helper;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.sales.Category;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.util.*;

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
                        return Title.generateKeywordsMap(final_goods);
                    }
                });
        String keyWords = (keywordsMap == null) ? "" : StringUtils.trimToEmpty(keywordsMap.get("goodsKeywords"));
        return "优惠券,优惠券网,代金券," + keyWords;
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
        }
        return keywordsMap;
    }

}
