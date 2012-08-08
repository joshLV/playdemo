package models;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.PointGoods;
import play.Play;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-8
 * Time: 下午1:59
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "point_goods_orders")
public class PointGoodsOrder extends Model {

    public static String EMAIL_RECEIVER = Play.configuration.getProperty("goods_not_enough.receiver", "dev@uhuila.com");
    public static final BigDecimal FREIGHT = new BigDecimal("6");
    private static final String DECIMAL_FORMAT = "0000000";

    // 兑换用户ID
    @Column(name = "user_id")
    public long userId;

    // 兑换用户类型
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    public AccountType userType;

    // 兑换订单号
    @Column(name = "order_no")
    public String orderNumber;

    // 兑换订单状态
    @Enumerated(EnumType.STRING)
    public PointGoodsOrderStatus status;

    // 订单总积分数
    public BigDecimal amount;

    @Column(name = "point_Price")
    public BigDecimal pointPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_goods_id", nullable = true)
    public PointGoods pointGoods;

    @Column(name = "point_goods_name")
    public String pointGoodsName;

    @Column(name = "buy_number")
    public Long buyNumber;

    @Column(name = "buyer_phone")
    public String buyerPhone;

    @Column(name = "buyer_mobile")
    public String buyerMobile;

    public String remark;

    @Column(name = "receiver_phone")
    public String receiverPhone;

    @Column(name = "receiver_mobile")
    public String receiverMobile;

    @Column(name = "receiver_address")
    public String receiverAddress;

    @Column(name = "receiver_name")
    public String receiverName;

    public String postcode;

    // 兑换申请时间
    @Column(name = "apply_at")
    public Date applyAt;

    // 兑换审核通过时间
    @Column(name = "accept_at")
    public Date acceptAt;

    // 审核员ID
    @Column(name = "operate_user_id")
    public String operateUserId;

    // 积分退还时间
    @Column(name = "refund_at")
    public Date refundAt;

    // 订单更新时间
    @Column(name = "updated_at")
    public Date updatedAt;

    // 乐观锁
    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    // 订单描述
    public String description;

    // 逻辑删除,0:未删除，1:已删除
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "delivery_no")
    public String deliveryNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    public DeliveryType deliveryType;

    @Column(name = "web_identification_id")
    public Long webIdentificationId;

    @Transient
    public String searchKey;

    @Transient
    public String searchItems;

    @Column(name = "delivery_company")
    public String deliveryCompany;

    public PointGoodsOrder() {

    }

    public static final String CACHEKEY = "POINTGOODSORDER";
    public static final String CACHEKEY_BASEUSERID = "POINTGOODSORDER_USERID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);
        super._delete();
    }

    private PointGoodsOrder(long userId, AccountType userType) {
        this.userId = userId;
        this.userType = userType;

        if (userType == AccountType.CONSUMER) {
            User user = User.findById(userId);
            UserInfo userInfo = UserInfo.findByUser(user);
            if (userInfo != null) {
                this.buyerPhone = userInfo.phone;
            }
            this.buyerMobile = user.mobile;
        }

        this.status = PointGoodsOrderStatus.APPLY;
        this.deleted = DeletedStatus.UN_DELETED;
        this.orderNumber = generateOrderNumber();
        this.applyAt = new Date();
        this.amount = BigDecimal.ZERO;

        this.lockVersion = 0;

        this.updatedAt = new Date();
    }

    /**
     * 设置订单地址
     *
     * @param address 地址
     */
    public void setAddress(Address address) {
        if (address != null) {
            this.receiverAddress = address.getFullAddress();
            this.receiverMobile = address.mobile;
            this.receiverName = address.name;
            this.receiverPhone = address.getPhone();
            this.postcode = address.postcode;
        }

    }

    public void setUser(long userId, AccountType accountType) {
        this.userId = userId;
        this.userType = accountType;
        this.save();
    }


    /**
     * 生成订单编号.
     *
     * @return 订单编号
     */
    public static String generateOrderNumber() {
        String numberHeader = String.valueOf(Integer.parseInt(String.format("%tj", new Date())) % 8 + 1);
        for (int i = 0; i < 100000; i++) {
            int random = new Random().nextInt(10000000);
            //使用7位的格式化工具对数字进行补零
            DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
            String orderNumber = numberHeader + decimalFormat.format(random);
            PointGoodsOrder order = PointGoodsOrder.find("byOrderNumber", orderNumber).first();
            if (order == null) {
                return orderNumber;
            }
        }
        throw new RuntimeException("still could not generate an unique order number after 100000 tries");
    }

    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param user    会员ID
     * @param pointGoodsId 商品ID
     * @param number  购买数量
     * @return
     */
    public static Boolean checkLimitNumber(User user, Long pointGoodsId, Long boughtNumber, int number) {

        //取出商品的限购数量
        PointGoods goods = PointGoods.findById(pointGoodsId);
        int limitNumber = 0;
        if (goods.limitNumber != null) {
            limitNumber = goods.limitNumber;
        }

        //超过限购数量,则表示已经购买过该商品
        if (limitNumber > 0 && (number > limitNumber || limitNumber <= boughtNumber)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public void checkInventory(PointGoods pointGoods, long number) throws NotEnoughInventoryException {
        if (pointGoods.baseSale < number) {
            throw new NotEnoughInventoryException();
        }
    }

    /**
     * 订单查询
     *
     * @param condition  订单查询条件
     * @param pageNumber 第几页
     * @param pageSize   每页记录
     * @return ordersPage 订单信息
     */
    public static JPAExtPaginator<PointGoodsOrder> query(PointGoodsOrderCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<PointGoodsOrder> orderPage = new JPAExtPaginator<>
                ("Order o", "o", PointGoodsOrder.class, condition.getFilter(),
                        condition.paramsMap)
                .orderBy(condition.getOrderByExpress());
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
        orderPage.setBoundaryControlsEnabled(true);
        return orderPage;
    }

    /**
     * 订单审核不通过，增加库存，减少销量
     */
    public void cancelAndUpdateOrder() {
        this.status = PointGoodsOrderStatus.CANCELED;
        this.updatedAt = new Date();
        pointGoods.baseSale += buyNumber;
        pointGoods.saleCount -= buyNumber;
        pointGoods.save();
        this.save();

    }

//    public void createAndUpdateInventory() {
//
//            orderItem.goods.baseSale -= orderItem.buyNumber;
//            orderItem.goods.saleCount += orderItem.buyNumber;
//            orderItem.goods.save();
//            orderItem.save();
//            if (orderItem.goods.materialType == MaterialType.REAL) {
//                haveFreight = true;
//            }
//            if (orderItem.goods.baseSale == 3 || orderItem.goods.baseSale == 0) {
//                //发送提醒邮件
//                MailMessage mailMessage = new MailMessage();
//                mailMessage.addRecipient(EMAIL_RECEIVER);
//                mailMessage.setSubject(Play.mode.isProd() ? "库存不足，商品即将下架" : "商品下架【测试】");
//                Supplier supplier = Supplier.findById(orderItem.goods.supplierId);
//                mailMessage.putParam("supplierName", supplier.fullName);
//                mailMessage.putParam("goodsName", orderItem.goods.name);
//                mailMessage.putParam("faceValue", orderItem.goods.faceValue);
//                mailMessage.putParam("baseSales", orderItem.goods.baseSale);
//                mailMessage.putParam("offSalesFlag", "noInventory");
//                MailUtil.sendGoodsOffSalesMail(mailMessage);
//            }
//
//
//        if (haveFreight) {
//            addFreight();
//            save();
//        }
//    }

}
