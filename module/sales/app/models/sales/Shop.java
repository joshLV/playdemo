package models.sales;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import play.db.jpa.Model;


@Entity
@Table(name = "shops")
public class Shop extends Model {
	
	
    @Column(name = "company_id")
    public long companyId;

    @Column(name = "area_id")
    public long areaId;

    public String no;

    public String name;

    public String address;

    public String phone;

    public String traffic;

    @Column(name = "is_close")
    public String isClose;

    public float latitude;

    public float longitude;

    @Column(name = "created_at")
    public String createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_at")
    public String updatedAt;

    @Column(name = "updated_by")
    public String updatedBy;

    public int deleted;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "display_order")
    public String displayOrder;
    
    @ManyToMany(cascade={CascadeType.PERSIST},mappedBy="shops")//这里说明了关系维护端是shop，goods是关系被维护端 
    private Set<Goods> goods = new HashSet<Goods>(); 
    public Set<Goods> getGoods() {  
        return goods;  
    }  
  
    public void setGoods(Set<Goods> goods) {  
        this.goods = goods;  
    }  
    
    @Override  
    public int hashCode() {  
        final int prime = 31;  
        int result = 1;  
        result = prime * result + ((id == null) ? 0 : id.hashCode());  
        return result;  
    }  
  
    @Override  
    public boolean equals(Object obj) {  
        if (this == obj)  
            return true;  
        if (obj == null)  
            return false;  
        if (getClass() != obj.getClass())  
            return false;  
        Shop other = (Shop) obj;  
        if (id == null) {  
            if (other.id != null)  
                return false;  
        } else if (!id.equals(other.id))  
            return false;  
        return true;  
    }  
    
    /**
     * 读取某商户的全部门店记录
     *
     * @param companyId
     * @return
     */
    public static List<Shop> findShopByCompany(long companyId) {
        return Shop.find("company_id=? and deleted=0", companyId).fetch();
    }

    /**
     * 虚拟删除
     *
     * @param id
     * @return
     */
    public static boolean deleted(long id) {
        Shop shop = Shop.findById(id);
        shop.deleted = 1;
        shop.save();
        return true;
    }

}
