package models.sales;

import java.util.List;

/**
 * 门店moduls 类
 * @author xuefuwei
 *
 */

public class Shops {


    /**
     * 读取某商户的全部门店记录
     * @param companyId
     * @return 
     */
    public List<Shop> findShopByConpany(long companyId){
        List<Shop> list = Shop.find("company_id=? and deleted=0",companyId).fetch();
        return list;
    }
    
    /**
     * 虚拟删除
     * @param id
     * @return
     */
    public boolean deleted(long id){
        Shop shop  = Shop.findById(id);
        shop.deleted = 1;
        shop.save();
        return true;
    }
    
    /**
     * 物理删除
     * @param id
     * @return
     */
    public boolean delete(long id){
        Shop shop  = Shop.findById(id);
        shop.delete();
        return true;
    }
    
    
    
    
}
