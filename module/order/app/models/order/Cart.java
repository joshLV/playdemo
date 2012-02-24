package models.order;

import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    
    public static List<Cart> findAll(User user, String cookie, MaterialType type) {
        if (user == null && cookie == null && type == null) {
            return new ArrayList<Cart>();
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder("SELECT NEW Cart(c.goods, SUM(c.number)) from Cart c where ( 1=2 ");
        if(user != null)   sql.append("or c.user = :user ");
        if(cookie != null) sql.append("or c.cookieIdentity = :cookie ");
        sql.append(") ");
        if(type != null) sql.append("and c.goods.materialType = :type ");
        sql.append(" group by c.goods");
        
        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        query.setParameter("user", user);
        if(user != null)   query.setParameter("user",user);
        if(cookie != null) query.setParameter("cookie", cookie);
        if(type != null)   query.setParameter("type", type);
        
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
            cartAmount = cartAmount.add(cart.goods.salePrice.multiply(new BigDecimal(cart.number)));
        }
        return cartAmount;
    }

    public static void clear(User user, String cookieIdentity) {
        List<Cart> cartList = Cart.find("user=? or cookieIdentity=?", user, cookieIdentity).fetch();
        for (Cart cart : cartList) {
            cart.delete();
        }
    }
}
