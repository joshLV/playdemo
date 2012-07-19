package models.job.resaler;

import models.yihaodian.YihaodianJobUtil;
import models.yihaodian.YihaodianOrder;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 */
//@Every("1h")
public class YihaodianJobScanner extends Job {
    @Override
    public void doJob(){
        Query query = JPA.em().createQuery("select y from YihaodianOrder y where y.pendingActions <> :actions and y.createdAt < :createdAt");

        query.setParameter("actions", "");

        //一小时之前
        Long millis = System.currentTimeMillis() - 3600000;
        Date date = new Date(millis);
        query.setParameter("createdAt", date);

        List<YihaodianOrder> orders = query.getResultList();
        for(YihaodianOrder order : orders) {
            YihaodianJobUtil.addJob(order.getId());
        }
    }
}
