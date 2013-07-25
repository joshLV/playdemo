package controllers;

import com.uhuila.common.util.DateUtil;
import models.operator.Operator;
import models.order.ChannelAccountCheckingDetail;
import models.resale.Resaler;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.text.ParseException;
import java.util.ArrayList;
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

    public static void wubaExcelOut(ChannelAccountCheckingDetail bill) throws ParseException {
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "58对账清单_" + DateUtil.dateToString(bill.startDay,
                "yyyy-MM-dd") + "-" + DateUtil.dateToString(bill.endDay, "yyyy-MM-dd") + ".xls");
        int days = DateUtil.daysBetween(bill.startDay, bill.endDay) + 1;
        List<ChannelAccountCheckingDetail> thirdBills = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String date = DateUtil.dateToString(DateHelper.afterDays(bill.startDay, i),
                    "yyyy-MM-dd");
            List<ChannelAccountCheckingDetail> tempThirdBills = WubaUtil.createWubaAccountCheckingDetail(date);
            thirdBills.addAll(tempThirdBills);
        }
        render(thirdBills);
    }
}
