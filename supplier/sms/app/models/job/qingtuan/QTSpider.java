package models.job.qingtuan;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.IO;
import play.libs.WS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author likang
 *         定时抓取青团的商品API
 *         每天凌晨三点执行
 *         Date: 12-11-8
 */
@JobDefine(title = "定时抓取青团的商品API", description = "定时抓取青团的商品API")
// @On("0 0 3 * * ?")
public class QTSpider extends JobWithHistory {
    private static final String GATEWAY = "http://www.tsingtuan.com/api/team.php";
    private static final int PAGE_SIZE = 200;

    private static Supplier supplier = null;
    private static Brand brand = null;

    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");

    @Override
    public void doJobWithHistory() {

        int offset = 0;
        while (true) {
            WS.HttpResponse response = WS.url(GATEWAY
                    + "?size=" + PAGE_SIZE
                    + "&offset=" + offset
                    + "&iscoupon=Y").get();//只倒入电子券

            offset += PAGE_SIZE;
            int totalCount = parseQtXml(response.getString());
            if (totalCount != PAGE_SIZE) {
                break;
            }
        }
    }

    /**
     * 解析青团返回的xml数据
     *
     * @param xmlString 青团xml数据
     * @return xml中包含的子条目数
     */
    public static int parseQtXml(String xmlString) {
        supplier = Supplier.find("byDomainName", "tsingtuan").first();
        brand = Brand.find("bySupplier", supplier).first();
        if (supplier == null || brand == null) {
            Logger.error("qingtuan spider error: no supplier(tsingtuan) or brand found");
            return 0;
        }

        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlString);
        } catch (DocumentException e) {
            Logger.error("failed to parse QingTuan request");
            return 0;
        }

        Element root = document.getRootElement();
        List<Element> urlElements = (List<Element>) root.elements();
        for (Element urlElement : urlElements) {
            Element teamElement = urlElement.element("team");
            if (teamElement == null) {
                continue;
            }
            Long teamId = Long.parseLong(teamElement.elementTextTrim("team_id"));
            Goods goods = Goods.find("bySupplierGoodsId", teamId).first();
            if (goods != null) {
                continue;
            }
            createGoods(teamElement);
        }

        return urlElements.size();
    }

    /**
     * 创建一百券商品
     *
     * @param element 清团的team element
     */
    private static void createGoods(Element element) {
        Goods goods = new Goods();

        Shop shop = createShop(element);
        if (shop == null) {
            Logger.error("qingtuan create goods failed: can not find the area: %s", element.elementTextTrim("citys"));
            return;
        }
        goods.shops = new HashSet<Shop>();
        goods.shops.add(shop);

        Category category = getCategory(element);
        if (category == null) {
            Logger.error("qingtuan find category failed: %s - %s",
                    element.elementTextTrim("group"), element.elementTextTrim("promotion"));
            return;
        }
        goods.categories = new HashSet<>();
        goods.categories.add(category);


        //餐饮类 6% 其他 8%
        goods.createdAt = new Date();
        goods.createdBy = supplier.fullName;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.setDetails(element.elementText("detail"));
        goods.effectiveAt = new Date(Long.parseLong(element.elementTextTrim("begin_time")) * 1000);
        goods.expireAt = new Date(Long.parseLong(element.elementTextTrim("expire_time")) * 1000);
        goods.faceValue = new BigDecimal(element.elementTextTrim("market_price"));
        goods.salePrice = new BigDecimal(element.elementTextTrim("team_price"));
        goods.status = GoodsStatus.APPLY;
        goods.cumulativeStocks = 9999L;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.updatedAt = new Date();
        goods.couponType = GoodsCouponType.GENERATE;
        goods.promoterPrice = BigDecimal.ZERO;
        goods.isAllShop = false;

        //如果是餐饮的
        if (goods.getParentCategoryIds().equals("1")) {
            goods.originalPrice = goods.salePrice.multiply(new BigDecimal("0.94")).setScale(2, RoundingMode.FLOOR);
        } else {
            goods.originalPrice = goods.salePrice.multiply(new BigDecimal("0.92")).setScale(2, RoundingMode.FLOOR);
        }

        goods.setDiscount(goods.salePrice.multiply(BigDecimal.TEN).divide(goods.faceValue, RoundingMode.FLOOR).setScale(2, RoundingMode.FLOOR));
        goods.resaleAddPrice = BigDecimal.ZERO;
        goods.materialType = MaterialType.ELECTRONIC;
        goods.virtualBaseSaleCount = Long.parseLong(element.elementTextTrim("pre_number"));

        goods.name = element.elementTextTrim("title");
        goods.shortName = element.elementTextTrim("product");
        goods.title = goods.shortName;
        goods.setExhibition(Goods.replaceWithOurImage(element.elementText("summary")));
        goods.setDetails(Goods.replaceWithOurImage(element.elementText("detail")));
        goods.setPrompt(Goods.replaceWithOurImage(element.elementText("notice")));

        goods.supplierGoodsId = Long.parseLong(element.elementTextTrim("team_id"));
        goods.supplierId = supplier.id;

        goods.brand = brand;
        String imageUrl = element.elementTextTrim("image");
        if (!StringUtils.isBlank(imageUrl) && !Play.runingInTestMode()) {
            InputStream is = WS.url(imageUrl).get().getStream();
            try {
                File file = File.createTempFile("qingtuan", "." + FilenameUtils.getExtension(imageUrl));
                IO.write(is, file);
                goods.imagePath = uploadFile(file);
            } catch (IOException e) {
                Logger.error("upload file error:", e);
            }
        }
        goods.save();
    }

    private static Shop createShop(Element element) {
        String areaName = element.elementTextTrim("citys");
        Area area = Area.find("byName", areaName).first();
        if (area == null) {
            return null;
        }
        Shop shop = new Shop();
        shop.supplierId = supplier.id;
        shop.areaId = area.id;
        shop.cityId = area.id;
        shop.name = area.name;
        shop.address = element.elementTextTrim("address");
        shop.phone = element.elementTextTrim("partner_phone");
        String longLat = element.elementTextTrim("longlat");
        if (StringUtils.isNotBlank(longLat)) {
            String tmp[] = longLat.split(",");
            if (tmp.length > 0) {
                shop.longitude = tmp[0];
            }
            if (tmp.length > 1) {
                shop.latitude = tmp[1];
            }
        }
        return shop.save();
    }

    private static Category getCategory(Element element) {
        String subCategoryKey = "qingtuan." + element.elementTextTrim("group") + "." + element.elementTextTrim("promotion");
        String scgIdStr = Messages.get(subCategoryKey);
        if (scgIdStr.equals(subCategoryKey)) {
            scgIdStr = "1032";//未找到分类的就放在 生活服务->其他
        }

        return Category.findById(Long.parseLong(scgIdStr));
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
