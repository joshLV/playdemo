package models.sales;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "brands")
public class Brand extends Model {
    public String name;
    public String logo;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    public Category category;
}
