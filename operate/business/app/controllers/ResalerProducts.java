package controllers;

import models.jingdong.groupbuy.JDGroupBuyHelper;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import models.operator.OperateUser;
import com.uhuila.common.constants.DeletedStatus;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import models.sales.ResalerProductJournal;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 * Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class ResalerProducts extends Controller {
    public static int PAGE_SIZE = 20;

    /**
     * 分销商品列表
     */
    @ActiveNavigation("resale_partner_product")
    public static void index(GoodsCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new GoodsCondition();
        }
        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        Map<String, List<ResalerProduct> > partnerProducts = new HashMap<>();
        for(models.sales.Goods goods : goodsPage.getCurrentPage()) {
            List<ResalerProduct> products = ResalerProduct.find("byGoodsAndDeleted", goods, DeletedStatus.UN_DELETED).fetch();
            for (ResalerProduct product : products) {
                List<ResalerProduct> p = partnerProducts.get(goods.id + product.partner.toString());
                if (p == null) {
                    p = new ArrayList<>();
                    partnerProducts.put(goods.id + product.partner.toString(), p);
                }
                p.add(product);
            }
        }

        render(goodsPage, condition, supplierList, partnerProducts);
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    /**
     * 查看该分销商品的编辑历史
     */
    @ActiveNavigation("resale_partner_product")
    public static void journal(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        List<ResalerProductJournal> journals = ResalerProductJournal.find("byProduct", product).fetch();
        for(ResalerProductJournal journal : journals) {
            journal.operator = ((OperateUser)OperateUser.findById(journal.operatorId)).userName;
        }
        render(journals);
    }

    /**
     * 查看某一次具体编辑动作
     */
    @ActiveNavigation("resale_partner_product")
    public static void journalJson(Long journalId) {
        ResalerProductJournal journal = ResalerProductJournal.findById(journalId);
        if (journal == null) {
            notFound();
        }
        render(journal);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(String partner, Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            Logger.info("goods not found");
            error("商品不存在");
        }
        List<ResalerProduct> products = ResalerProduct.find("goods = ? and partner = ? and deleted = ? order by createdAt desc",
                goods, OuterOrderPartner.valueOf(partner.toUpperCase()), DeletedStatus.UN_DELETED).fetch();
        for (ResalerProduct product : products) {
            if (product.creatorId != null) product.creator = ((OperateUser)OperateUser.findById(product.creatorId)).userName;
            if (product.lastModifierId != null) product.lastModifier = ((OperateUser)OperateUser.findById(product.lastModifierId)).userName;
        }
        render(products);
    }

    @ActiveNavigation("resale_partner_product")
    public static void delete(Long id) {
        ResalerProduct product = ResalerProduct.findById(id);
        if (product == null) {
            error("分销商品不存在");
        }
        product.deleted = DeletedStatus.DELETED;
        product.updatedAt = new Date();
        product.lastModifier(OperateRbac.currentUser().id).save();
        showProducts(product.partner.toString().toLowerCase(), product.goods.id);
    }

    /**
     * 录入分销商品的第三方ID和url
     */
    @ActiveNavigation("resale_partner_product")
    public static void enter(Long productId, String partnerPid, String url, Date endSale) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (!StringUtils.isBlank(partnerPid)){
            ResalerProduct t = ResalerProduct.find("byPartnerAndPartnerProductId", product.partner, partnerPid).first();
            if(t != null) {
                error("该分销商品ID已存在");
            }

            product.partnerProductId = partnerPid;
        }
        if (!StringUtils.isBlank(url)) product.url = url;
        if (endSale != null) {
            endSale = new Date(endSale.getTime() + 24*60*60*1000 - 1000);
            product.endSale = endSale;
        }
        product.save();

        showProducts(product.partner.toString().toLowerCase(), product.goods.id);
    }
    /**
     * 完整录入分销商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void add(ResalerProduct product) {
        if (product.goods == null) {
            error("一百券商品不存在，请重新填写一百券商品ID");
        }

        ResalerProduct t = ResalerProduct.find("byPartnerAndPartnerProductId", product.partner, product.partnerProductId).first();
        if(t != null) {
            error("该分销商品ID已存在");
        }

        product.goodsLinkId = product.goods.id;
        product.createdAt = new Date();
        product.updatedAt = new Date();
        product.status = ResalerProductStatus.UPLOADED;
        product.deleted = DeletedStatus.UN_DELETED;
        product.creator(OperateRbac.currentUser().id);
        product.save();

        showProducts(product.partner.toString().toLowerCase(), product.goods.id);
    }

    /**
     * 查看即将过期的商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void expiringGoods() {
        List<ResalerProduct> products = ResalerProduct.find("deleted = ? and endSale is not null and (partner = ?) order by endSale",
                DeletedStatus.UN_DELETED, OuterOrderPartner.JD).fetch();
        render(products);
    }

    /**
     * 延期一个商品 并返回延期结果
     * 自动延期到【次月月底】和【券过期前5天】中比较早的一个日期.
     *
     * @param id 分销商品ID
     */
    public static void autoDelay(Long id) {
        ResalerProduct product = ResalerProduct.findById(id);
        if (product == null) {
            renderJSON("{\"error\":\"商品不存在\"}");
        }
        boolean success = false;
        //首先算下月最后一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endSale = calendar.getTime();

        //然后算券过期前5天
        calendar.setTime(product.goods.expireAt);
        calendar.add(Calendar.DAY_OF_MONTH, -5);

        if (calendar.before(endSale)) {
            endSale = calendar.getTime();
        }



        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (product.partner == OuterOrderPartner.JD) {
            Map<String, Object> params = new HashMap<>();
            params.put("venderTeamId", product.goodsLinkId);
            params.put("jdTeamId", product.partnerProductId);
            params.put("saleEndDate", format.format(endSale));
            JingdongMessage response = JDGroupBuyUtil.sendRequest("teamExtension", params);
            success = response.isOk();

        }else {
            renderJSON("{\"error\":\"不支持\"}");
        }

        if (success) {
            renderJSON("{\"endSale\":\"" + format.format(endSale) +  "\"}");
        }else {
            renderJSON("{\"error\":\"延期失败\"}");
        }
    }
}
