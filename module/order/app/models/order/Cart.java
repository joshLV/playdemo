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

    public String cookieIdentity;

    @ManyToOne
    public Goods goods;


    public int number;

    public int lockVersion;

    public Date createdAt;

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
