package jobs.dadong;

import models.dadong.DadongProduct;
import models.sales.Goods;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;
import utils.SafeParse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步大东商品，返回同步商品数。
 *
 * 使用Job作为异步请求机制.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:24
 */
public class DadongProductsSyncRequest {

    public static Integer syncProducts() {

        String origanCode = Play.configuration.getProperty("dadong.origin.code", "shanghaishihui_201301145784");
        String url = Play.configuration.getProperty("dadong.url", "http://www.ddrtty.net/bjskiService.action");

        List<DadongProduct> dadongProductList = new ArrayList<>();

        Template template = TemplateLoader.load("app/xml/template/dadong/GetProductRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("origan_code", origanCode);
        int pageIndex = 0;
        boolean nextLoop = true;
        do {
            args.put("page_index", pageIndex);
            String xml = template.render(args);
            Map<String, Object> params = new HashMap<>();
            params.put("xml", xml);
            WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

            System.out.println("get result : " + pageIndex);
            try {
                Document document = client.postXml("thirdtuan.dadang.GetProducts", url, params, String.valueOf(pageIndex));

                List<Node> products = XPath.selectNodes("//products", document);

                if (products == null || products.size() == 0) {
                    Logger.info("找不到products");
                    break;
                }
                for (Node node : products) {

                    String productId = XPath.selectText("product_id", node);
                    System.out.println("productId=" + productId);
                    DadongProduct product = new DadongProduct();
                    product.productId = SafeParse.toLong(productId);
                    product.province = XPath.selectText("province", node);
                    product.city = XPath.selectText("city", node);
                    product.aqeg = XPath.selectText("aqeg", node);
                    product.category = XPath.selectText("category", node);
                    product.productName = XPath.selectText("product_name", node);
                    product.faceValue = SafeParse.toBigDecimal(XPath.selectText("product_faceValue", node));
                    product.webValue = SafeParse.toBigDecimal(XPath.selectText("product_webValue", node));
                    product.platformValue = SafeParse.toBigDecimal(XPath.selectText("product_platformValue", node));
                    product.ticketExplain = XPath.selectText("product_ticketExplain", node);
                    product.address = XPath.selectText("product_address", node);
                    product.imageUrl = XPath.selectText("product_image", node);
                    product.expireTime = SafeParse.toDate(XPath.selectText("product_expireTime", node));

                    dadongProductList.add(product);
                }
            } catch (Exception e) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>");
                e.printStackTrace();
                nextLoop = false;
            }
            pageIndex ++;

        } while (nextLoop);

        List<Goods> goodsList = new ArrayList<>();

        for (DadongProduct product : dadongProductList) {
            Goods goods = Goods.find("bySupplierGoodsId", product.productId).first();
            if(goods != null){
                continue;
            }
            createGoods(product);
        }

        return dadongProductList.size();
    }

    private static void createGoods(DadongProduct product) {
        
    }

}
