package models.consumer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.Unique;
import play.db.jpa.Model;

@Entity
@Table(name = "user_point_config")
public class UserPointConfig extends Model {
    
    private static final long serialVersionUID = 81232060911L;
    
	@Column(name = "point_type")
	public int pointType;

	@Column(name = "point_number")
	@Unique
	public String pointNumber;

	@Column(name = "deal_points")
	public int dealPoints;

	@Column(name = "point_note")
	public String pointNote;
	
	@Column(name = "point_title")
	public String pointTitle;
}
