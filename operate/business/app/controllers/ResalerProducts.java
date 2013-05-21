package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import models.operator.OperateUser;
import models.operator.Operator;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import models.sales.ResalerProductJournal;
import models.sales.ResalerProductStatus;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-8
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
            condition.operatorCode = Operator.defaultOperator().code;
        }
        condition.isLottery = false;
        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<Resaler> resalerList = Resaler.find("status =? and operator.code =? and partner is not null ", ResalerStatus.APPROVED, condition.operatorCode).fetch();
        Map<String, List<ResalerProduct>> partnerProducts = new HashMap<>();
        for (models.sales.Goods goods : goodsPage.getCurrentPage()) {
            List<ResalerProduct> products = ResalerProduct.find("goods = ? and status != ? and deleted = ? ",
                    goods, ResalerProductStatus.STAGING, DeletedStatus.UN_DELETED).fetch();
            for (ResalerProduct product : products) {
                List<ResalerProduct> p = partnerProducts.get(goods.id + "-"+product.resaler.id.toString());
                if (p == null) {
                    p = new ArrayList<>();
                    partnerProducts.put(goods.id + "-"+product.resaler.id.toString(), p);
                }
                p.add(product);
            }
        }

        List<Operator> operators = Operator.findUnDeleted();

        render(goodsPage, operators, condition, supplierList, partnerProducts, resalerList);
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
        for (ResalerProductJournal journal : journals) {
            journal.operator = ((OperateUser) OperateUser.findById(journal.operatorId)).userName;
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
    public static void showProducts(String partner, Long goodsId, String loginName) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            Logger.info("goods not found");
            error("商品不存在");
        }
        StringBuilder sql = new StringBuilder("select r from ResalerProduct r " +
                "where goods = :goods and partner= :partner and status <> :status and deleted = :deleted");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("goods", goods);
        params.put("partner", OuterOrderPartner.valueOf(partner.toUpperCase()));
        params.put("status", ResalerProductStatus.STAGING);
        params.put("deleted", DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and resaler.loginName = :loginName");
            params.put("loginName", loginName);
        }

        Query query = JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<ResalerProduct> products = query.getResultList();
        for (ResalerProduct product : products) {
            if (product.creatorId != null)
                product.creator = ((OperateUser) OperateUser.findById(product.creatorId)).userName;
            if (product.lastModifierId != null)
                product.lastModifier = ((OperateUser) OperateUser.findById(product.lastModifierId)).userName;
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
        showProducts(product.partner.toString().toLowerCase(), product.goods.id, product.resaler.loginName);
    }

    /**
     * 录入分销商品的第三方ID和url
     */
    @ActiveNavigation("resale_partner_product")
    public static void enter(Long productId, String partnerPid, String url, Date endSale) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (!StringUtils.isBlank(partnerPid)) {
            ResalerProduct t = ResalerProduct.find("byPartnerAndPartnerProductId", product.partner, partnerPid).first();
            if (t != null) {
                error("该分销商品ID已存在");
            }

            product.partnerProductId = partnerPid;
        }
        if (!StringUtils.isBlank(url)) product.url = url;
        if (endSale != null) {
            endSale = new Date(endSale.getTime() + 24 * 60 * 60 * 1000 - 1000);
            product.endSale = endSale;
        }
        product.save();

        showProducts(product.partner.toString().toLowerCase(), product.goods.id, product.resaler.loginName);
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
        if (t != null) {
            error("该分销商品ID已存在");
        }

        product.goodsLinkId = product.goods.id;
        product.createdAt = new Date();
        product.updatedAt = new Date();
        product.status = ResalerProductStatus.UPLOADED;
        product.deleted = DeletedStatus.UN_DELETED;
        product.creator(OperateRbac.currentUser().id);
        product.save();

        showProducts(product.partner.toString().toLowerCase(), product.goods.id, product.resaler.loginName);
    }

    /**
     * 查看即将过期的商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void expiringGoods() {
        Date startOfToday = DateUtils.truncate(new Date(), Calendar.DATE);
        Date fourDaysLater = DateUtils.addDays(startOfToday, 4);

        List<ResalerProduct> products = ResalerProduct.find(
                "deleted = ? and status != ? and endSale is not null and endSale < ? and (partner = ?) " +
                        "and goods.materialType = ? order by endSale",
                DeletedStatus.UN_DELETED, ResalerProductStatus.STAGING, fourDaysLater,
                OuterOrderPartner.JD, MaterialType.ELECTRONIC).fetch();
        render(products);
    }

    /**
     * 延期一个商品 并返回延期结果
     * 自动延期到【次月月底】和【券过期前5天】中比较早的一个日期.
     *
     * @param id 分销商品ID
     */
    @ActiveNavigation("resale_partner_product")
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
        Date endSale = DateUtils.ceiling(calendar.getTime(), Calendar.DATE);

        //然后算券过期前5天
        calendar.setTime(product.goods.expireAt);
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        calendar = DateUtils.ceiling(calendar, Calendar.DATE);

        if (calendar.before(endSale)) {
            endSale = calendar.getTime();
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (product.partner) {
            case JD:
                Map<String, Object> params = new HashMap<>();
                params.put("venderTeamId", product.goodsLinkId);
                params.put("jdTeamId", product.partnerProductId);
                params.put("saleEndDate", format.format(endSale));
                product.endSale = endSale;
                product.save();
                JingdongMessage response = JDGroupBuyUtil.sendRequest("teamExtension", params);
                success = response.isOk();
                break;
            default:
                renderJSON("{\"error\":\"暂不支持\"}");
        }

        if (success) {
            renderJSON("{\"endSale\":\"" + format.format(endSale) + "\"}");
        } else {
            renderJSON("{\"error\":\"延期失败\"}");
        }
    }
}
