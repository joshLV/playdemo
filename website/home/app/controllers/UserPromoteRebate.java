package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.PromoteRebate;
import models.order.PromoteRebateCondition;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-4
 * Time: 上午11:38
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class UserPromoteRebate extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index() {
        User user = SecureCAS.getUser();
        //产生推荐码
        user.generatePromoterCode();
        //取得推荐产生的返利金额
        PromoteRebate promoteRebate = PromoteRebate.getRebateAmount(user);
        //取得推荐购买金额
        BigDecimal boughtAmount = Order.getBoughtPromoteRebateAmount(user.id);
        //取得推荐购买并消费额的金额
        BigDecimal consumedAmount = ECoupon.getConsumedPromoteRebateAmount(user.id);
        render(user, promoteRebate, boughtAmount, consumedAmount);

    }

    public static void rank() {
        render();
    }

    public static void account(PromoteRebateCondition condition) {
        User user = SecureCAS.getUser();
        //产生推荐码
        user.generatePromoterCode();
        //取得推荐产生的返利金额
        PromoteRebate promoteRebate = PromoteRebate.getRebateAmount(user);
        //取得推荐购买金额
        BigDecimal boughtAmount = Order.getBoughtPromoteRebateAmount(user.id);
        //取得推荐购买并消费额的金额
        BigDecimal consumedAmount = ECoupon.getConsumedPromoteRebateAmount(user.id);

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new PromoteRebateCondition();
        }
        JPAExtPaginator<PromoteRebate> accountList = PromoteRebate.findAccounts(user, condition, pageNumber, PAGE_SIZE);

        render(user, promoteRebate, boughtAmount, consumedAmount, accountList);
    }


}
