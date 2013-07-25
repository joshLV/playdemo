package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.supplier.SupplierInjector;
import models.order.GiftCard;
import models.order.GiftCardCondition;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-7-16
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierGiftCards extends Controller {
    private static final int PAGE_SIZE = 20;
    public static void index(GiftCardCondition condition) {

        Long supplierId = SupplierRbac.currentUser().supplier.id;

        Query query = JPA.em().createQuery("select distinct(g) from Goods g, GoodsProperty gp " +
                "where g.id = gp.goodsId and gp.name=:pname and gp.value=:pvalue and g.supplierId = :supplierId");
        query.setParameter("pname", Goods.PROPERTY_GIFT_CARD);
        query.setParameter("pvalue", "1");
        query.setParameter("supplierId", supplierId);

        List<Goods> goodsList = query.getResultList();

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new GiftCardCondition();
        }

        if (condition.goods != null) {
            if (!condition.goods.equals(supplierId)) {
                error("无效的商品.");
            }
        }

        JPAExtPaginator<GiftCard> giftCards = GiftCard.findByCondition(condition,
                pageNumber, PAGE_SIZE);


        LinkedHashMap<String, String> trans = new LinkedHashMap<>();
        trans.put("username", "姓名");
        trans.put("mobile", "手机号");
        trans.put("address", "地址");
        trans.put("postcode", "邮编");
        trans.put("date", "配送日期");
        trans.put("message", "留言");

        JsonParser parser = new JsonParser();
        for (GiftCard giftCard : giftCards.getCurrentPage()) {
            if (giftCard.userInput != null) {
                JsonElement element = parser.parse(giftCard.userInput);
                StringBuilder message = new StringBuilder();
                JsonObject jsonObject = element.getAsJsonObject();
                for(Map.Entry<String, String> entry : trans.entrySet()) {
                    String key = entry.getKey();
                    if (jsonObject.has(key)) {
                        message.append(trans.get(key) + " : " + jsonObject.get(key).getAsString() + "<br/>");
                    }
                }
                giftCard.userInput = message.toString();
            }
        }
        render(goodsList, giftCards);
    }

    public static void send(String company, String number) {

    }
}
