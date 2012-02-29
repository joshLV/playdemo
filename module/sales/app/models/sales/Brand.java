package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "brands")
public class Brand extends Model {
    public String name;
    public String logo;
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
}
