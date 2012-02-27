package models.order;

import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "cart")
public class Cart extends Model {
    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;

    public long number;

    @Column(name = "cookie_identity")
    public String cookieIdentity;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
    
    public Cart(Goods goods, long number){
        this.goods = goods;
        this.number = number;
    }

    public Cart(User user, String cookieIdentity, Goods goods, long number) {
        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods = goods;
        this.number = number;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }
    
    public static Cart order(User user, String cookie, 
            Goods goods, int increment) {
        if ((user == null && cookie == null) || goods == null) {
            return null;
        }

        Cart cart;
        if (user != null ) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        } else {
            cart = Cart.find("byCookieIdentityAndGoods", cookie, goods).first();
        }

        //如果记录已存在，则更新记录，否则新建购物车记录
        if (cart != null){
            if (cart.number + increment > 0) {
                cart.number += increment;
                cart.save();
                return cart;
            } else {
                //不允许存在数量小于等于0的购物车记录
                return null;
            }
        } else {
            if (increment <= 0){
                return null;
            }
            if (user != null){
                return new Cart(user, null, goods, increment).save(); 
            } else {
                return new Cart(null, cookie, goods, increment).save();
            }
        }
            
    }

    public static int delete(User user, String cookie, List<Long> goodsIds) {
        if(user == null && cookie == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("delete from Cart c where (1=2");
        Map<String,Object> params = new HashMap<>();
        if (user !=null ) {
            sql.append(" or c.user = :user" );
            params.put("user", user);
        }
        if (cookie != null ) {
            sql.append(" or c.cookieIdentity = :cookie");
            params.put("cookie", cookie);
        }
        sql.append(")");
        if (goodsIds != null){
            sql.append(" and c.goods.id in :ids");
            params.put("ids", goodsIds);
        }

        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        for (Map.Entry<String,Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.executeUpdate();
    }

    public static List<Cart> findAll(User user, String cookie, 
            MaterialType type) {
        if (user == null && cookie == null && type == null) {
            return new ArrayList<Cart>();
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder(
            "select new Cart(c.goods, SUM(c.number)) from Cart c where ( 1=2 ");
        Map<String,Object> params = new HashMap<>();

        if (user != null) {
            sql.append("or c.user = :user ");
            params.put("user", user);
        }
        if (cookie != null) {
            sql.append("or c.cookieIdentity = :cookie ");
            params.put("cookie", cookie);
        }
        sql.append(") ");
        if (type != null) {
            sql.append("and c.goods.materialType = :type ");
            params.put("type", type);
        }
        sql.append(" group by c.goods");
        
        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        for (Map.Entry<String,Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        
        return query.getResultList();
    }
    
    public static List<Cart> findAll(User user, String cookie){
        return findAll(user, cookie, null);
    }
    
    public static List<Cart> findECart(User user, String cookie) {
        return findAll(user, cookie, MaterialType.ELECTRONIC);
    }

    public static List<Cart> findRCart(User user, String cookie) {
        return findAll(user, cookie, MaterialType.REAL);
    }

    public static BigDecimal amount(List<Cart> cartList) {
        BigDecimal cartAmount = new BigDecimal(0);
        for (Cart cart : cartList) {
            cartAmount = cartAmount.add(
                    cart.goods.salePrice.multiply(new BigDecimal(cart.number)));
        }
        return cartAmount;
    }

    public static void clear(User user, String cookie) {
        delete(user, cookie, null);
    }
}
