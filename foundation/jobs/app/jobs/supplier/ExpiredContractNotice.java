package jobs.supplier;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.Order;
import models.order.OrderStatus;
import models.supplier.ContractStatus;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.On;
import play.jobs.OnApplicationStart;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-8-20
 * Time: 下午5:07
 */
@JobDefine(title = "商户合同预警检查", description = "10天内商户合同过期提醒")
@On("0 0 15 * * ?")
public class ExpiredContractNotice extends JobWithHistory {
    public static String MAIL_RECEIVER = Play.configuration.getProperty("expired.contract.email.receiver", "dev@uhuila.com");

    @Override
    public void doJobWithHistory() {
        String sql = "select sc from SupplierContract sc where sc.status = :status and sc.expireAt >=:expireAtBegin and " +
                " sc.expireAt <:expireAtEnd group by sc.supplierId order by sc.description,sc.expire_at ";
        Query query = Order.em().createQuery(sql);
        query.setParameter("status", ContractStatus.NORMAL);
        query.setParameter("expireAtBegin", DateUtils.truncate(new Date(), Calendar.DATE));
        query.setParameter("expireAtEnd", DateUtils.truncate(DateUtils.addDays(new Date(), 10), Calendar.DATE));
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<SupplierContract> contracts = query.getResultList();
        Map<String, Object> contractMap;
        List<Map<String, Object>> contractList = new ArrayList<>();
        String subject = "商户合同到期提醒";
        for (SupplierContract contract : contracts) {
            contractMap = new HashMap<>();
            contractMap.put("supplierCompanyName", contract.supplierCompanyName);
            contractMap.put("supplierName", contract.supplierName);
            contractMap.put("supplierId", contract.supplierId);
            contractMap.put("description", contract.description);
            contractMap.put("expireAt", new SimpleDateFormat("yyyy-MM-dd").
                    format(contract.expireAt));
            contractMap.put("contractCount", contracts.size());
            contractList.add(contractMap);
        }
        if (contracts.size() > 0) {
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient(MAIL_RECEIVER.split(","));

            mailMessage.setSubject(Play.mode.isProd() ? subject : subject + "【测试】");

            mailMessage.putParam("contractList", contractList);
            mailMessage.putParam("contractCount", contracts.size());
            MailUtil.sendExpiredContractNoticeMail(mailMessage);
        }

    }
}
