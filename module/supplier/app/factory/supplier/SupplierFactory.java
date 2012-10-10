package factory.supplier;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午4:46
 */

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import models.sales.SecKillGoodsItem;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import util.DateHelper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        supplier.loginName = "tom";
        supplier.status=  SupplierStatus.NORMAL;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            supplier.createdAt =  dateFormat.parse("2012-02-29 16:33:18");
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return supplier;
    }

}


