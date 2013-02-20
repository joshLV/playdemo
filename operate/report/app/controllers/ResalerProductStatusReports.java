package controllers;

import models.ResalerProductReportCondition;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import operate.rbac.annotations.ActiveNavigation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableConverter;
import utils.CrossTableUtil;

import javax.persistence.Query;
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
public class ResalerProductStatusReports extends Controller {
    public static void index(ResalerProductReportCondition condition) {

        if (condition == null) {
            condition = new ResalerProductReportCondition();
        }
        if (condition.partners == null) {
            condition.partners = new ArrayList<>();
        }

        List<ResalerProduct> resultList = getChannelGoods(condition);

        List<Map<String, Object>> reportPage = CrossTableUtil.generateCrossTable(resultList, converter);
        render(reportPage, condition);
    }


    private static CrossTableConverter<ResalerProduct, ResalerProduct> converter =
            new CrossTableConverter<ResalerProduct, ResalerProduct>() {
        @Override
        public String getRowKey(ResalerProduct target) {
            return target.goods.shortName + "※"  +
                   target.goods.code + "※" +
                   target.goods.getStatus() + "※" +
                   target.goods.id;
        }

        @Override
        public String getColumnKey(ResalerProduct target) {
            return target.partner.toString();
        }

        @Override
        public ResalerProduct addValue(ResalerProduct target, ResalerProduct oldValue) {
            if (target == null) {
                return oldValue;
            }
            if (oldValue == null) {
                return target;
            }
            if (oldValue.status == ResalerProductStatus.ONSALE) {
                return oldValue;
            }

            return target;
        }
    };

    private static List<ResalerProduct> getChannelGoods(ResalerProductReportCondition condition) {
        Query query = JPA.em().createQuery("select c from ResalerProduct c " + condition.filter());

        for (Map.Entry<String, Object> param : condition.getParamMap().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        List<ResalerProduct> onlyOnSaleList = query.getResultList();
        return onlyOnSaleList;
    }
}
