package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.solr.SolrField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 13-5-15
 * Time: 上午11:12
 */
@Entity
@Table(name = "freights")
public class Freight extends Model {

    @Required
    public String province;

    @Required
    public BigDecimal price;

    @Required
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Required
    @ManyToOne
    @JoinColumn(name = "express_id")
    public ExpressCompany express;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @SolrField
    public Date createdAt;

    /**
     * 计费方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "freight_type")
    public FreightType type;


    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    @SolrField
    public Date updatedAt;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    @SolrField
    public DeletedStatus deleted;

    public static final String OTHER_PROVICE = "其他";

    @Override
    public boolean create() {
        deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        createdAt = new Date();
        return super.create();
    }

    public static void update(Long id, Freight freight) {
        Freight updatedFreight = Freight.findById(id);
        updatedFreight.refresh();
        updatedFreight.price = freight.price;
        updatedFreight.province = freight.province;
        updatedFreight.updatedAt = new Date();
        updatedFreight.save();
    }

    public static void delete(long id) {
        Freight freight = Freight.findById(id);
        if (freight == null) {
            return;
        }
        if (!com.uhuila.common.constants.DeletedStatus.DELETED.equals(freight.deleted)) {
            freight.deleted = com.uhuila.common.constants.DeletedStatus.DELETED;
            freight.save();
        }
    }

    public static List<Freight> findUnDeleted() {
        return find("deleted=? order by createdAt DESC", com.uhuila.common.constants.DeletedStatus.UN_DELETED).fetch();
    }

    public static BigDecimal findFreight(Supplier supplier, ExpressCompany express, String address) {
        Freight freightRule = findFreightRule(supplier, express, address);
        return freightRule.price;
    }

    public static Freight findFreightRule(Supplier supplier, ExpressCompany express, String address) {
        List<Freight> freightList = Freight.find("supplier = ? and express = ?", supplier, express).fetch();

        int index = Integer.MAX_VALUE;
        Freight targetFreight = null;
        for (Freight freight: freightList) {
            int i = address.indexOf(freight.province);
            if (i >= 0 && i < index) {
                index = i;
                targetFreight = freight;
            }
        }

        //如果没有找到 并且没有设置默认的运费 抛出异常
        if (index == Integer.MAX_VALUE) {
            Freight freight = Freight.find("supplier = ? and express = ? and province = ?",
                    supplier, express, Freight.OTHER_PROVICE).first();
            if (freight != null ) {
                targetFreight = freight;
            }else {
                Logger.error("freight is not existed: supplierId:" + supplier.id + " express:" + express.id + " address:" + address);
                throw new RuntimeException("freight is not existed when finding freight");
            }
        }

        return targetFreight;  //To change body of created methods use File | Settings | File Templates.
    }


}
