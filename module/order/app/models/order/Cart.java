package models.order;

import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    @Column(name = "cookie_identity")
    public String cookieIdentity;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    public Cart(User user, String cookieIdentity, Goods goods, long number, MaterialType materialType) {
        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods = goods;
        this.number = number;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
        this.materialType = materialType;
    }

    public static List<Cart> findECart() {
        return Cart.find("byMaterialType", MaterialType.Electronic).fetch();
    }

    public static List<Cart> findRCart() {
        return Cart.find("byMaterialType", MaterialType.Real).fetch();
    }

    public static List<Cart> findECart(String cartCookieId) {
        return Cart.find("cookieIdentity=? and materialType = ?", cartCookieId, MaterialType.Electronic).fetch();
    }

    public static List<Cart> findRCart(String cartCookieId) {
        return Cart.find("cookieIdentity=? and materialType = ?", cartCookieId, "Real").fetch();
    }
    
    public static List<Cart> findByCookie(String cartCookieId){
        return Cart.find("cookieIdentity=?",cartCookieId).fetch();
    }


    public static BigDecimal amount(List<Cart> cartList) {
        BigDecimal cartAmount = new BigDecimal(0);
        for (Cart cart : cartList) {
            cartAmount = cart.goods.salePrice.multiply(new BigDecimal(cart.number)).add(cartAmount);
        }
        return cartAmount;
    }

}
