package models.sales;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "goods_shops")
public class Goods_shops extends Model {
	public Long good_id;
	public Long shop_id;
}
