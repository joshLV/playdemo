package models.order;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import models.sales.Goods;
import models.consumer.*;

@Entity
@Table(name = "cart")
public class Cart extends Model {
    @ManyToOne
    public User user;    

    @ManyToOne
    public Goods goods;

    public int number;

    @Column(name="cookie_identity")
    public String cookieIdentity;

    @Column(name="lock_version")
    public int lockVersion;

    @Column(name="created_at")
    public Date createdAt;

    @Column(name="updated_at")
    public Date updatedAt;

    public Cart(User user, String cookieIdentity, Goods goods, int number) {
        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods= goods;
        this.number= number;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }
}
