package jobs.dadong;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.dadong.DadongProduct;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.libs.WS;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;
import utils.SafeParse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 同步大东商品，返回同步商品数。
 * <p/>
 * 使用Job作为异步请求机制.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:24
 */
public class DadongProductsSyncRequest {
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String ORGAN_CODE = Play.configuration.getProperty("dadong.origin.code",
            "shanghaishihui_201301145784");
    public static String URL = Play.configuration.getProperty("dadong.url", "http://www.ddrtty.net/bjskiService.action");

    public static Integer syncProducts() {

        Supplier dadong = Supplier.findByDomainName("dadong");


        List<DadongProduct> dadongProductList = new ArrayList<>();

        Template template = TemplateLoader.load("app/xml/template/dadong/GetProductRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("organCode", ORGAN_CODE);
        int pageIndex = 0;
        boolean nextLoop = true;
        do {
            args.put("pageIndex", pageIndex);
            String xml = template.render(args);
            Map<String, Object> params = new HashMap<>();
            params.put("xml", xml);
            WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

            System.out.println("get result : " + pageIndex);
            try {
                Document document = client.postXml("thirdtuan.dadang.GetProducts", URL, params,
                        String.valueOf(pageIndex));

                List<Node> products = XPath.selectNodes("//products", document);

                if (products == null || products.size() == 0) {
                    Logger.info("找不到products");
                    break;
                }
                for (Node node : products) {
                    String productId = XPath.selectText("product_id", node);
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

                    Logger.info("product:" + product.toString());
                    dadongProductList.add(product);
                }
            } catch (Exception e) {
                e.printStackTrace();
                nextLoop = false;
            }
            pageIndex++;

        } while (nextLoop);

        List<Goods> goodsList = new ArrayList<>();

        for (DadongProduct product : dadongProductList) {
            Goods goods = Goods.find("bySupplierGoodsId", product.productId).first();
            if (goods != null) {
                continue;
            }
            createGoods(dadong, product);
        }

        return dadongProductList.size();
    }

    /**
     * 创建一百券商品
     *
     * @param product 大东商品
     */
    private static void createGoods(Supplier dadong, DadongProduct product) {
        Goods goods = new Goods();
        Brand brand = Brand.find("bySupplier", dadong).first();

        Shop shop = createShop(dadong, product);
        if (shop == null) {
            Logger.error("dadong create goods failed: can not find the area: %s", product.address);
            //return;
        } else {
            goods.shops = new HashSet<Shop>();
            goods.shops.add(shop);
        }

        Category category = Category.find("name like '旅游票务%'").first();
        if (category == null) {
            Logger.error("dadong find category failed: 旅游票务");
            //return;
        } else {
            goods.categories = new HashSet<>();
            goods.categories.add(category);
        }


        //餐饮类 6% 其他 8%
        goods.createdAt = new Date();
        goods.createdBy = dadong.fullName;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.setDetails(product.ticketExplain);
        goods.effectiveAt = new Date();
        goods.beginOnSaleAt = new Date();
        goods.endOnSaleAt = product.expireTime;
        goods.expireAt = product.expireTime;
        goods.faceValue = product.faceValue;
        goods.salePrice = product.webValue;
        goods.originalPrice = product.platformValue;
        goods.status = GoodsStatus.APPLY;
        goods.cumulativeStocks = 9999L;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.updatedAt = new Date();
        goods.couponType = GoodsCouponType.GENERATE;
        goods.promoterPrice = BigDecimal.ZERO;
        goods.isAllShop = false;

        goods.setDiscount(goods.salePrice.multiply(BigDecimal.TEN).divide(goods.faceValue, RoundingMode.FLOOR).setScale(2, RoundingMode.FLOOR));
        goods.resaleAddPrice = BigDecimal.ZERO;
        goods.materialType = MaterialType.ELECTRONIC;
        goods.virtualBaseSaleCount = 0l;

        goods.name = product.productName;
        goods.shortName = product.productName;
        goods.title = goods.shortName;
        goods.setExhibition("说明");
        goods.setPrompt("提示");

        goods.supplierGoodsId = product.productId;
        goods.supplierId = dadong.id;

        goods.brand = brand;
        String imageUrl = product.imageUrl;
        if (!StringUtils.isBlank(imageUrl) && !Play.runingInTestMode()) {
            InputStream is = WS.url(imageUrl).get().getStream();
            try {
                File file = File.createTempFile("dadong", "." + FilenameUtils.getExtension(imageUrl));
                IO.write(is, file);
                goods.imagePath = uploadFile(file);
            } catch (IOException e) {
                Logger.error("upload file error:", e);
            }
        }
        goods.save();
    }

    private static Shop createShop(Supplier dadong, DadongProduct product) {
        Area area = Area.find("byName", product.aqeg).first();
        Shop shop = new Shop();
        if (area != null) {
            shop.areaId = area.id;
            shop.cityId = area.id;
            shop.name = area.name;
        }
        shop.supplierId = dadong.id;
        shop.transport = product.province + " " + product.city + " " + product.aqeg;
        shop.address = product.address;
        return shop.save();
    }

    private static String uploadFile(File file) {
        String targetFilePath = null;
        try {
            targetFilePath = FileUploadUtil.storeImage(file, ROOT_PATH);
        } catch (IOException e) {
            return null;
        }
        return targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
    }
}
