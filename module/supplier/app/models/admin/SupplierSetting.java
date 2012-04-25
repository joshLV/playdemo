package models.admin;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;
@Entity
@Table(name = "supplier_setting")
public class SupplierSetting extends Model {
	@Column(name = "supplierUser_key")
	public Long supplierUserKey;

	@Column(name = "supplierUser_shop_id")
	public Long supplierUserShopId;

	@Column(name = "supplierUser_shop_value")
	public String supplierUserShopValue;

	@Column(name = "created_At")
	public Date createdAt;

	public void save(Long supplierUserId, Long shopId, String shopName) {
		SupplierSetting SupplierSetting = getSetting(supplierUserId);
		Long id = 0L;
		if (SupplierSetting !=  null) {
			id =SupplierSetting.supplierUserShopId;
		}
		
		if (shopId != null && id != shopId) {
			supplierUserKey = supplierUserId;
			supplierUserShopId = shopId;
			supplierUserShopValue = shopName;
			createdAt =  new Date();
			this.save();
		}
	}

	public static SupplierSetting getSetting(Long supplierUserId) {
		List<SupplierSetting> supplierList = SupplierSetting.find("select s from SupplierSetting s where s.supplierUserKey = ? order by createdAt desc", supplierUserId).fetch();
		SupplierSetting set = null;
		if (supplierList.size()>0) {
			set = supplierList.get(0);
		}

		return set;
	}

}
