package models.resale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

@Entity
@Table(name = "resaler_cart")
public class ResalerCart extends Model {
    @ManyToOne
    public Resaler resaler;

    @ManyToOne
    public Goods goods;

    public long number;

    public String phone;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;


    private static Pattern phonePattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"); 

    public ResalerCart(Resaler resaler, Goods goods, String phone, long number) {
        this.resaler = resaler;
        this.goods = goods;
        this.number = number;
        this.phone = phone;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    public static List<String> batchOrder(Resaler resaler, Goods goods, List<String> phones) {
        if (resaler == null || goods == null || phones == null){
            return null;
        }
        List<String> invalidPhones = new ArrayList<>();
        for(String phone : phones){
            ResalerCart resalerCart = reorder(resaler, goods, phone, 1);
            if(resalerCart == null ) {
                invalidPhones.add(phone);
            }
        }
        return invalidPhones;
    }

    /**
     * 加入或修改购物车列表
     *
     * @param resaler      用户
     * @param phone     手机号
     * @param goods     商品
     * @param increment 购物车中商品数增量，
     *                  若购物车中无此商品，则新建条目
     *                  若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */

    public static ResalerCart reorder(Resaler resaler, Goods goods, String phone, int increment) {
        if (resaler == null || goods == null || phone == null || !phonePattern.matcher(phone).matches()) {
            return null;
        }

        ResalerCart cart = ResalerCart.find("byResalerAndGoodsAndPhone", resaler, goods, phone).first();

        //如果记录已存在，则更新记录，否则新建购物车记录
        if (cart != null) {
            if (cart.number + increment > 0) {
                cart.number += increment;
                cart.updatedAt = new Date();
                cart.save();
                return cart;
            } else {
                //不允许存在数量小于等于0的购物车记录
                return null;
            }
        } else {
            if (increment <= 0) {
                return null;
            }
            return new ResalerCart(resaler, goods, phone, increment).save();
        }

    }

    /**
     * 从购物车中删除指定商品，所有该商品所对应的条目均被删除
     *
     * @param resaler     用户
     * @param goods       商品
     * @return 成功删除的数量
     */
    public static int delete(Resaler resaler, Goods goods) {
        List<ResalerCart> carts = ResalerCart.find("byResalerAndGoods", resaler, goods).fetch();
        for (ResalerCart cart : carts) {
            cart.delete();
        }
        return carts.size();
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param resaler     用户
     * @param goods       商品列表，若未指定，则删除该用户所有的购物车条目
     * @param phone       手机号
     * @return 成功删除的购物车条目
     */
    public static ResalerCart delete(Resaler resaler, Goods goods, String phone) {

        ResalerCart cart = ResalerCart.find("byResalerAndGoodsAndPhone", resaler, goods, phone).first();
        if (cart == null ){
            return null;
        }
        cart.delete();
        return cart;
    }

    /**
     * 列出所有符合条件的购物车条目，合并数量后输出
     *
     * @param resaler   用户
     * @return 合并数量后的购物车条目列表
     */
    public static List<List<ResalerCart>> findAll(Resaler resaler) {
        if (resaler == null ) {
            return new ArrayList<List<ResalerCart>>();
        }
        List<ResalerCart> carts = ResalerCart.find(
                "select r from ResalerCart r where r.resaler = ? order by r.createdAt desc", resaler).fetch();
        List<List<ResalerCart>> result = new ArrayList<>();
        for (ResalerCart cart : carts) {
            boolean found = false;
            for (List<ResalerCart> rcl : result) {
                if(rcl.get(0).goods.id == cart.goods.id){
                    rcl.add(cart);
                    found = true;
                    break;
                }
            }
            if (!found){
                List<ResalerCart> rcl = new ArrayList<ResalerCart>();
                rcl.add(cart);
                result.add(rcl);

            }
        }
        return result;
    }

    

    /**
     * 清除用户购物车中所有条目
     */
    public static int clear(Resaler resaler) {
        if (resaler == null) {
            return 0;
        }
        List<ResalerCart> carts = ResalerCart.find("byResaler", resaler).fetch();
        for (ResalerCart cart : carts) {
            cart.delete();
        }
        return carts.size();
    }
}
