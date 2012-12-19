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
 * 金币发放job(每月1号凌晨1点执行).
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@On("0 0 1 1 * ?")
public class SendGoldenCoinsJob extends Job {
    @Override
    public void doJob() {
        String sql = "select new models.sales.CheckinRelations(count(u.id),user) from UserGoldenCoin u where u.createdAt >=:createdAtBegin and u.createdAt <=:createdAtEnd group by u.user";
        Query query = JPA.em().createQuery(sql);
        query.setParameter("createdAtBegin", DateUtil.lastMonthOfFirstDay());
        query.setParameter("createdAtEnd", DateUtil.lastMonthOfEndDay());
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<CheckinRelations> resultList = query.getResultList();
        for (CheckinRelations goldenCoins : resultList) {
            if (UserGoldenCoin.getCheckinInfo(goldenCoins.user, true) == null) {
                if (goldenCoins.number >= 30) {
                    new UserGoldenCoin(goldenCoins.user, Long.valueOf("350"), null, "奖励：350金币", true).save();
                } else if (goldenCoins.number >= 20) {
                    new UserGoldenCoin(goldenCoins.user, Long.valueOf("100"), null, "奖励：100金币", true).save();
                }
            }
        }
    }

}
