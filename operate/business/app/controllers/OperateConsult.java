package controllers;

import models.sales.ConsultRecord;
import models.sales.ConsultResultCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;

/**
 * User: wangjia
 * Date: 12-9-24
 * Time: 下午12:37
 */
@With(OperateRbac.class)
@ActiveNavigation("crm_search_consumers")
public class OperateConsult extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(ConsultResultCondition condition) {
        if (condition == null) {
            condition = new ConsultResultCondition();
        }
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<ConsultRecord> consultList = models.sales.ConsultRecord.query(condition, null, pageNumber, PAGE_SIZE);

        render(consultList, condition);
    }

    public static void consultExcelOut(ConsultResultCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new ConsultResultCondition();
        }
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "咨询内容列表_" + System.currentTimeMillis() + ".xls");

        JPAExtPaginator<ConsultRecord> consultList = ConsultRecord.query(condition, null, pageNumber, PAGE_SIZE);
        for (ConsultRecord consult : consultList) {
            consult.consultTypeInfo = Messages.get("consult."+consult.consultType);
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            consult.createdAtInfo = formatter.format(consult.createdAt);
        }

        render(consultList, condition);

    }
}
