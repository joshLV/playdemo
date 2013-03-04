package models.job;

import com.uhuila.common.util.DateUtil;
import models.consumer.UserGoldenCoin;
import models.sales.CheckinRelations;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.On;

import javax.persistence.Query;
import java.util.List;

/**
 * 金币发放job(每月1号凌晨3点执行).
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@On("0 0 3 1 * ?")
public class SendGoldenCoinsJob extends Job {
    @Override
    public void doJob() {
        String sql = "select new models.sales.CheckinRelations(count(u.id),user) from UserGoldenCoin u where u.isPresent = 0 and u.createdAt >=:createdAtBegin and u.createdAt <=:createdAtEnd group by u.user";
        Query query = JPA.em().createQuery(sql);
        query.setParameter("createdAtBegin", DateUtil.lastMonthOfFirstDay());
        query.setParameter("createdAtEnd", DateUtil.lastMonthOfEndDay());
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<CheckinRelations> resultList = query.getResultList();
        for (CheckinRelations goldenCoins : resultList) {
            if (goldenCoins.checkinTimes < 20) {
                continue;
            }
            if (goldenCoins.checkinTimes >= 30) {
                UserGoldenCoin.createAwardIfNotExist(goldenCoins.user, 350L);
            } else if (goldenCoins.checkinTimes >= 20) {
                UserGoldenCoin.createAwardIfNotExist(goldenCoins.user, 100L);
            }
        }
    }

}
