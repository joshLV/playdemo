package models.sales;

import java.util.List;

import play.db.jpa.Model;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


@Entity
@Table(name="shops")
public class Shop extends Model {

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
    
    
    /**
     * 读取某商户的全部门店记录
     * @param companyId
     * @return 
     */
    public static List<Shop> findShopByCompany(long companyId){
        List<Shop> list = Shop.find("company_id=? and deleted=0",companyId).fetch();
        return list;
    }
    
    /**
     * 虚拟删除
     * @param id
     * @return
     */
    public static boolean deleted(long id){
        Shop shop  = Shop.findById(id);
        shop.deleted = 1;
        shop.save();
        return true;
    }
    
}
