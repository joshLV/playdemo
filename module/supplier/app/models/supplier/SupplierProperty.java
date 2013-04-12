package models.supplier;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * User: yan
 * Date: 13-4-10
 * Time: 下午5:28
 */
@Entity
@Table(name = "supplier_property")
public class SupplierProperty extends Model {
    /**
     * 商户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = true)
    public Supplier supplier;

    /**
     * 关键字
     */
    @Column(name = "property_name")
    public String name;

    /**
     * 值
     */
    @Column(name = "property_value")
    public String value;

    public SupplierProperty(Supplier supplier, String name, String value) {
        this.supplier = supplier;
        this.name = name;
        this.value = value;
    }

    /**
     * 取得指定商户的菜单控制信息
     */
    public static SupplierProperty findProperty(Supplier supplier, String propertyName) {
        return SupplierProperty.find("supplier=? and name=?", supplier, propertyName).first();
    }
}
