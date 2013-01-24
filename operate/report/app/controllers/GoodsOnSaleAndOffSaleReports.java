package controllers;

import models.GoodsOnSaleAndOffSaleCondition;
import models.GoodsOnSaleAndOffSaleReport;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-21
 * Time: 下午4:27
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_status_reports")
public class GoodsOnSaleAndOffSaleReports extends Controller {
    public static void index(GoodsOnSaleAndOffSaleCondition condition) {

        if (condition == null) {
            condition = new GoodsOnSaleAndOffSaleCondition();
        }
        if (condition.resaleIds == null) {
            condition.resaleIds = new ArrayList<>();
        }
        Map<String, String[]> params = request.params.all();

        List<GoodsOnSaleAndOffSaleReport> resalerList = GoodsOnSaleAndOffSaleReport.findByStatus(condition);

        for (GoodsOnSaleAndOffSaleReport report : resalerList) {
            String[] idArray = params.get("resaleIds" + report.resalerId.toString());
            if (idArray != null) {
                condition.resaleIds.add(Long.valueOf(idArray[0]));
            }
        }

        renderArgs.put("resalerList", resalerList);

        List<GoodsOnSaleAndOffSaleReport> resultList = GoodsOnSaleAndOffSaleReport.getChannelGoods(condition);

        List<Map<String, Object>> reportPage = CrossTableUtil.generateCrossTable(resultList, GoodsOnSaleAndOffSaleReport.converter);
        render(reportPage, condition);

    }

}
