package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.util.List;

/**
 * @author likang
 *         Date: 12-7-30
 */
@OnApplicationStart(async = true)
public class SendDailyReportJob extends Job{
    @Override
    public void doJob(){
        List<Supplier> suppliers = Supplier.find("byStatusAndDeleted", SupplierStatus.NORMAL, DeletedStatus.UN_DELETED).fetch();
        for(Supplier supplier : suppliers) {
        }
    }
}
