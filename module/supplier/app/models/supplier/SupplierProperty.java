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
    @Column(name="supplier_key")
    public String key;

    /**
     * 值
     */
    @Column(name = "supplier_value")
    public boolean value = false;

    public SupplierProperty(Supplier supplier, String key, boolean value) {
        this.supplier = supplier;
        this.key = key;
        this.value = value;
    }

    /**
     * 取得指定商户的菜单控制信息
     */
    public static SupplierProperty findByKey(Long id, String key) {
        return SupplierProperty.find("supplier.id=? and key=?", id, key).first();
    }
}
