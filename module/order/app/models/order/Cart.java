package models.order;

import models.consumer.User;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart extends Model {
    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;

    public int number;
    @Column(name = "material_type")
    public String materialType;

    @Column(name = "cookie_identity")
    public String cookieIdentity;

    @Column(name = "lockVersion")
    public int lockVersion;

    @Column(name = "createdAt")
    public Date createdAt;

    @Column(name = "updatedAt")
    public Date updatedAt;

    public Cart(User user, String cookieIdentity, Goods goods, int number, String materialType) {
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
        return Cart.find("byMaterialType", "e").fetch();
    }

    public static List<Cart> findRCart() {
        return Cart.find("byMaterialType", "r").fetch();
    }

    public static List<Cart> findECart(String cartCookieId) {
        return Cart.find("cookie_identity=? and materialType = ?", cartCookieId, "e").fetch();
    }

    public static List<Cart> findRCart(String cartCookieId) {
        return Cart.find("cookie_identity=? and materialType = ?", cartCookieId, "r").fetch();
    }
}
