package factory.supplier;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午4:46
 */

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import models.supplier.Supplier;

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
        supplier.createdAt = new Date();
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.domainName = "test";
        supplier.loginName = "tom";
        return supplier;
    }

}
