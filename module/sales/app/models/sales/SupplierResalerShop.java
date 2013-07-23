package models.sales;

import models.resale.Resaler;
import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * User: yan
 * Date: 13-7-23
 * Time: 下午3:57
 */
@Table(name = "supplier_resaler_shops")
@Entity
public class SupplierResalerShop extends Model {
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @ManyToOne
    @JoinColumn(name="resaler_id")
    public Resaler resaler;

    @Column(name = "resaler_partner_shop_id")
    public String resalerPartnerShopId;

    @Column(name = "resaler_partner_shop_name")
    public String resalerPartnerShopName;

    @Column(name = "login_name")
    public String loginName;

    @Column(name = "password")
    public String password;

    @Column(name = "cookie_value")
    public String cookieValue;

}
