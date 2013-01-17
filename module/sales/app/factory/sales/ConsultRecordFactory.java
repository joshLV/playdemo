package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.ConsultRecord;
import models.sales.ConsultType;

import java.util.Date;

/**
 * User: wangjia
 * Date: 12-9-25
 * Time: 上午11:47
 */
public class ConsultRecordFactory  extends ModelFactory<ConsultRecord> {
    @Override
    public ConsultRecord define() {
        ConsultRecord consult = new ConsultRecord();
        consult.phone="15026580827";
        consult.consultType= ConsultType.ORDERCONSULT;
        consult.deleted= DeletedStatus.UN_DELETED;
        consult.text="789798";
        consult.createdAt=new Date();
        consult.createdBy="admin";
        return consult;
    }
}
