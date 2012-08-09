package models;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.sales.GoodsStatus;
import models.sales.PointGoods;
import play.Play;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    // 兑换订单号
    @Column(name = "order_no")
    public String orderNumber;

    // 兑换订单状态
    @Enumerated(EnumType.STRING)
    public PointGoodsOrderStatus status;

    // 兑换数量
    @Column(name = "buy_number")
    public Long buyNumber;

    // 积分商品信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_goods_id", nullable = true)
    public PointGoods pointGoods;

    // 积分商品名称
    @Column(name = "point_goods_name")
    public String pointGoodsName;

    // 积分商品价格
    @Column(name = "point_Price")
    public Long pointPrice;

    // 订单总积分数
    public int amount;

    // 用户积分总数
    public int totalPoint;

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
    public Long operateUserId;

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

    public PointGoodsOrder(long userId, PointGoods pointGoods, Long buyNumber) throws NotEnoughInventoryException{
        checkInventory(pointGoods, buyNumber);
        this.userId = userId;
        User user = User.findById(userId);
        UserInfo userInfo = UserInfo.findByUser(user);
        if (userInfo != null) {
            this.buyerPhone = userInfo.phone;
        }
        this.buyerMobile = user.mobile;

        this.orderNumber = generateOrderNumber();
        this.buyNumber = buyNumber;
        this.pointGoods = pointGoods;
        this.pointPrice = pointGoods.pointPrice;
        this.pointGoodsName = pointGoods.name;
        this.setAmount();
        this.totalPoint = findUserTotalPoint(userId);
        this.status = PointGoodsOrderStatus.APPLY;
        this.deleted = DeletedStatus.UN_DELETED;
        this.applyAt = new Date();

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

    public void setUser(long userId) {
        this.userId = userId;
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

    /**
     * 查询库存是否充足
     * @param pointGoods
     * @param number
     * @throws NotEnoughInventoryException
     */
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
     * 订单审核不通过，增加库存，减少销量，返还积分
     */
    public void cancelAndUpdateOrder() {
        //返还积分
        int updatedPoint = totalPoint+amount;
        updateUserTotalPoint(userId,updatedPoint);
        this.totalPoint = findUserTotalPoint(userId);
        // 更新状态
        this.status = PointGoodsOrderStatus.CANCELED;
        this.refundAt = new Date();
        this.updatedAt = new Date();
        pointGoods.baseSale += buyNumber;
        pointGoods.saleCount -= buyNumber;
        pointGoods.save();

        this.save();

    }

    /**
     * 创建订单，减少库存，增加销量，扣除积分
     */
    public void createAndUpdateInventory() {
        // 扣除积分
        if (isAfford()){
            int updatedPoint = totalPoint-amount;
            updateUserTotalPoint(userId,updatedPoint);
            this.totalPoint = findUserTotalPoint(userId);

            pointGoods.baseSale -= buyNumber;
            pointGoods.saleCount += buyNumber;
            this.status = PointGoodsOrderStatus.APPLY;
            pointGoods.save();

            this.applyAt = new Date();
            this.updatedAt = new Date();
            this.save();
        }
        else{
            System.out.println("not afford");
        }

    }

    /**
     * 更改状态至申请已接受
     */
    public void accept(Long operateUserId){
        if (this.status != PointGoodsOrderStatus.APPLY){
            throw new RuntimeException("can not pay order:" + this.getId() + " since it's "+ this.status.toString());
        }
        else{
            this.status = PointGoodsOrderStatus.ACCEPT;
            this.acceptAt = new Date();
            this.updatedAt = new Date();
            this.operateUserId = operateUserId;
        }
    }

    /**
     * 设置 订单总积分数
     */
    public void setAmount(){
        if (pointPrice == null || buyNumber  == null){
            throw new ExceptionInInitializerError("Please check point price and buy number, are they null?");
        }
        else{
            this.amount =(int) (pointPrice*buyNumber) ;
        }
    }


    /**
     * 会员中心订单查询
     *
     * @param user       用户信息
     * @param condition  查询条件
     * @param pageNumber 第几页
     * @param pageSize   每页记录
     * @return ordersPage 订单信息
     */
    public static JPAExtPaginator<PointGoodsOrder> findUserOrders(User user, PointGoodsOrderCondition condition,
                                                        int pageNumber, int pageSize) {
        if (user == null) {
            user = new User();
        }
        JPAExtPaginator<PointGoodsOrder> orderPage = new JPAExtPaginator<>
                ("Order o", "o", PointGoodsOrder.class, condition.getFilter(user),
                        condition.paramsMap)
                .orderBy(condition.getUserOrderByExpress());
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
        return orderPage;
    }

    /**
     * 查询用户总积分
     * @param userId
     * @return  用户总积分
     */
    public static int findUserTotalPoint(Long userId) {
        UserInfo ui = UserInfo.find("byUser", User.findById(userId)).first();
        if (ui == null){
            return 0;
        }
        return ui.totalPoints;
    }

    /**
     * 更新用户总积分
     * @param userId
     * @param totalPoint
     * @throws Exception
     */
    public static void updateUserTotalPoint(Long userId,int totalPoint) {
        UserInfo ui = UserInfo.find("byUser", User.findById(userId)).first();
        if (ui == null){
            return;
        }
        ui.totalPoints = totalPoint;
        ui.save();
    }

    /**
     *
     * @return True 用户总积分足够完成兑换
     *         False 用户总积分不能完成兑换
     */
    public boolean isAfford(){
        boolean afford = true;
        if (totalPoint >= amount  ){
            return afford;
        }
        return !afford;
    }

}
