package factory.supplier;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午4:46
 */

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import util.DateHelper;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午4:20
 * To change this template use File | Settings | File Templates.
 */
public class SupplierFactory extends ModelFactory<Supplier> {

    @Override
    public Supplier define() {
        Supplier supplier = new Supplier();
        supplier.fullName = "Supplier0";
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.domainName = "localhost";
        supplier.status = SupplierStatus.NORMAL;
        supplier.otherName = "supplier";
        supplier.createdAt = DateHelper.beforeDays(new Date(), 30);
        supplier.logo = "/0/0/0/logo.jpg";
        return supplier;
    }

    @Factory(name = "KFC")
    public void defineKFC(Supplier supplier) {
        supplier.fullName = "肯德基";
    }

    @Factory(name = "qingtuan")
    public void defineQT(Supplier supplier) {
        supplier.domainName = "tsingtuan";
    }
}


