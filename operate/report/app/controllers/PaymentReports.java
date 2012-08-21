package controllers;

import models.order.Order;
import models.webop.PaymentReport;
import models.accounts.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableUtil;
import utils.PaginateUtil;

import javax.persistence.Query;
import java.util.*;

/**
 * @author likang
 *         Date: 12-8-2
 */
@With(OperateRbac.class)
public class PaymentReports extends Controller {
    private static final int PAGE_SIZE = 20;

    /**
     * 查询分销商资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("payment_reports")
    public static void index(AccountSequenceCondition condition) {
        if (condition == null) {
            condition = new AccountSequenceCondition();
            condition.createdAtBegin = new Date();
            condition.createdAtEnd = new Date();
        }
        List<Account> accounts = new ArrayList<>();
        accounts.add(PaymentReport.alipayAccount);
        accounts.add(PaymentReport.tenpayAccount);
        accounts.add(PaymentReport.kuaiqianAccount);
        condition.accounts = accounts;

        List<PaymentReport> resultList = PaymentReport.queryPaymentReport(condition);

        List<Map<String, Object>>  reportPage = CrossTableUtil.generateCrossTable(resultList, PaymentReport.converter);
//        List<Map<String, Object>>  report = CrossTableUtil.generateCrossTable(resultList, PaymentReport.converter);
        // 分页
//        ValuePaginator<Map<String, Object>> reportPage = PaginateUtil.wrapValuePaginator(report, pageNumber, PAGE_SIZE);
        render(reportPage, condition);
    }

    public static void detail(AccountSequenceCondition condition, Set<String> partners) {
        if (condition == null) {
            condition = new AccountSequenceCondition();
            condition.createdAtBegin = new Date();
            condition.createdAtEnd = new Date();
        }
        List<Account> accounts = new ArrayList<>();

        for(String partner : partners) {
            switch (partner){
                case "alipay":
                    accounts.add(PaymentReport.alipayAccount);
                    break;
                case "tenpay":
                    accounts.add(PaymentReport.tenpayAccount);
                    break;
                case "99bill":
                    accounts.add(PaymentReport.kuaiqianAccount);
                    break;
                default:
                    break;
            }
        }

        if(accounts.size() == 0) {
            error("no account specified");
        }
        condition.accounts = accounts;
        Query  query = AccountSequence.em().createQuery("select s from AccountSequence s where " + PaymentReport.processFilter(condition));
        for (Map.Entry<String, Object> param : condition.getParams().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        List<AccountSequence> sequences = query.getResultList();
        for(AccountSequence sequence : sequences) {
            setOrderInfo(sequence);
        }
        render(sequences);
    }

    private static void setOrderInfo(AccountSequence accountSequence) {
        if (accountSequence.orderId != null) {
            Order order = Order.findById(accountSequence.orderId);
            if (order != null) {
                accountSequence.payMethod = PaymentSource.findNameByCode(order.payMethod);
                accountSequence.orderNumber = order.orderNumber;
                accountSequence.remark = order.description;
            }
        }
    }
}
