package models.sales;

import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import play.db.jpa.JPA;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Query;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午6:19
 */
public class CheckinRelations {
    public Long checkinTimes;
    public User user;
    public String remarks;
    public Goods goods;
    public Long unUseNumber;
    public Long coinsNumber;

    public CheckinRelations() {
    }

    public CheckinRelations(Long checkinTimes, User user) {
        this.checkinTimes = checkinTimes;
        this.user = user;
    }

    public CheckinRelations(Long checkinTimes, User user, Goods goods, Long coinsNumber) {
        this.checkinTimes = checkinTimes;
        this.user = user;
        this.goods = goods;
        this.coinsNumber = coinsNumber;
    }

    public CheckinRelations(Long coinsNumber, Long unUseNumber) {
        this.coinsNumber = coinsNumber;
        this.unUseNumber = unUseNumber;
    }

    /**
     * 统计使用和未使用的金币数量
     *
     * @param reportPage
     * @return
     */
    public static CheckinRelations summary(JPAExtPaginator<UserGoldenCoin> reportPage) {
        if (reportPage == null || reportPage.size() == 0) {
            return new CheckinRelations(0l, 0l);
        }
        Long unused = 0L;
        Long used = 0L;
        for (UserGoldenCoin goldenCoin : reportPage) {
            if (goldenCoin.checkinNumber> 0) {
                used += goldenCoin.checkinNumber;
            } else {
                unused += goldenCoin.checkinNumber;
            }
        }
        return new CheckinRelations(unused, used);
    }


    /**
     * 计算签到的总人数
     *
     * @param resultList
     * @return
     */
    public static Long checkinSummary(List<CheckinRelations> resultList) {
        Long coinsNumber = 0l;
        for (CheckinRelations checkinRelations : resultList) {
            coinsNumber += checkinRelations.checkinTimes;
        }
        return coinsNumber;
    }

    /**
     * 取得签到的记录
     *
     * @param condition
     * @return
     */
    public static List<CheckinRelations> getCheckinList(UserCondition condition) {
        String sql = "select new models.sales.CheckinRelations(count(u.id),u.user,u.goods,sum(u.checkinNumber)) from UserGoldenCoin u where u.isPresent = false and u.checkinNumber>0 and" + condition.getCoinsReportCondition(null) + " group by u.user,u.goods";
        Query query = JPA.em().createQuery(sql);
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CheckinRelations> relationsList = query.getResultList();
        return relationsList;
    }
}
