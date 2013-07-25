package controllers;

import com.uhuila.common.util.DateUtil;
import models.operator.Operator;
import models.order.ChannelAccountCheckingDetail;
import models.resale.Resaler;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: wangjia
 * Date: 13-7-25
 * Time: 上午10:20
 */
@With(OperateRbac.class)
@ActiveNavigation("export_58_account_checking")
public class ExportChannelAccountChecking extends Controller {
    public static void index(ChannelAccountCheckingDetail bill) {
        List<Operator> operatorList = Operator.findShihui();
        List<Resaler> resalers = Resaler.findByChannelClearingBill(Operator.defaultOperator().id);
        render(operatorList, resalers, bill);

    }

    public static void export58(ChannelAccountCheckingDetail bill) {
        String startDay = DateUtil.dateToString(bill.startDay,"yyyy";
        String endDay = bill.endDay.toString();
        System.out.println("startDay = " + startDay);
//        createWubaAccountCheckingDetail
    }
}
