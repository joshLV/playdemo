package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "brands")
public class Brand extends Model {
    public String name;
    public String logo;
    @Column(name = "company_id")
    public Long companyId;
    @Column(name = "display_order")
    public int displayOrder;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    public Category category;

    public static List<Brand> findTop(int limit) {
        return find("order by displayOrder").fetch(limit);
    }

    public List<Brand> findTopByCategory(long categoryId, int limit) {
        return find("category=? order by displayOrder",
                new Category(categoryId)).fetch(limit);
    }


    public static List<Brand> findByCompanyId(long companyId) {
        return find("companyId=? order by displayOrder",companyId).fetch();
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
                    brands.remove(limit-1);
                }
                showBrands.addAll(brands);
                brands = showBrands;
            }
        }
        return brands;
    }

}
