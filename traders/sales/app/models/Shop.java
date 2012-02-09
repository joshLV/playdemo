package models;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="shops")
public class Shop extends Model {

    @Column(name="company_id")
    public long company_id;
    

    public long area_id;
    
    public String no;
    
    public String name;
    
    public String address;
    
    public String phone;
    
    public String traffic;
    
    public String is_close;
    
    public float latitude;
    
    public float longitude;
    
    public String created_at;
    
    public String created_by;
    
    public String updated_at;
    
    public String updated_by;
    
    public int deleted;
   
    public String lock_version;
    
    public String display_order;
    
}
