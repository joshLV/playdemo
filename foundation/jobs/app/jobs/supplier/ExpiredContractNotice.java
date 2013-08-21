package jobs.supplier;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.Order;
import models.order.OrderStatus;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import org.apache.commons.lang.time.DateUtils;
import play.Play;
import play.jobs.On;

import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-8-20
 * Time: 下午5:07
 */
@JobDefine(title = "商户合同预警检查", description = "10天后商户合同过期提醒")
@On("0 0 8 * * ?")
public class ExpiredContractNotice extends JobWithHistory {
    public static String MAIL_RECEIVER = Play.configuration.getProperty("expired.contract.email.receiver", "dev@uhuila.com");

    @Override
    public void doJobWithHistory() {
        String sql = "select sc from SupplierContract sc where sc.expireAt >=:expireAtBegin and "+
                " sc.expireAt <=:expireAtEnd group by sc.supplierId order by sc.id";
        Query query = Order.em().createQuery(sql);
        query.setParameter("expireAtBegin", DateUtils.truncate(new Date(), Calendar.DATE));
        query.setParameter("expireAtEnd", DateUtils.addDays(new Date(), 10));
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<SupplierContract> contracts = query.getResultList();
        String subject = "10天后商户合同过期提醒";
        if (contracts.size() > 0) {
            for (SupplierContract contract : contracts) {
                MailMessage mailMessage = new MailMessage();
                mailMessage.addRecipient(MAIL_RECEIVER);
                mailMessage.setSubject(Play.mode.isProd() ? subject : subject + "【测试】");
                mailMessage.putParam("expireAt", contract.expireAt);
                mailMessage.putParam("supplierCompanyName", contract.supplierCompanyName);
                mailMessage.putParam("supplierName", contract.supplierName);
                MailUtil.sendExpiredNoRefundCouponMail(mailMessage);
            }
        }

    }
}
