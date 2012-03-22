package models.sales;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
public class Brand extends Model {
    public String name;
    public String logo;
    @Column(name = "supplier_id")
    public Long supplierId;
    @Column(name = "display_order")
    public int displayOrder;

    public static List<Brand> findTop(int limit) {
        return find("order by displayOrder").fetch(limit);
    }

    public static List<Brand> findByOrder() {
        return find("order by displayOrder").fetch();
    }

    public static List<Brand> findTop(int limit, long brandId) {
        List<Brand> brands = findTop(limit);
        if (brandId != 0) {
            boolean containsBrands = false;
            for (Brand brand : brands) {
                if (brand.id == brandId) {
                    containsBrands = true;
                    break;
                }
            }
            if (!containsBrands) {
                List<Brand> showBrands = new ArrayList<>();
                showBrands.add((Brand) findById(brandId));
                if (brands.size() == limit) {
                    brands.remove(limit - 1);
                }
                showBrands.addAll(brands);
                brands = showBrands;
            }
        }
        return brands;
    }

}
