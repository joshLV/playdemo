package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import org.apache.commons.lang.StringUtils;
import play.jobs.Every;
import play.jobs.Job;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每小时查询各渠道商品的状态
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@Every("1h")
public class ScannerResalerProductStatusJob extends Job {

    @Override
    public void doJob() {
        Map<OuterOrderPartner, Resaler> resalers = new HashMap<>();
        resalers.put(OuterOrderPartner.DD, Resaler.findOneByLoginName(Resaler.DD_LOGIN_NAME));
        resalers.put(OuterOrderPartner.YHD, Resaler.findOneByLoginName(Resaler.YHD_LOGIN_NAME));
        resalers.put(OuterOrderPartner.WB, Resaler.findOneByLoginName(Resaler.WUBA_LOGIN_NAME));
        resalers.put(OuterOrderPartner.TB, Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME));
        resalers.put(OuterOrderPartner.JD, Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME));

        for (Map.Entry<OuterOrderPartner, Resaler> entry : resalers.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String onSaleKey = entry.getValue().onSaleKey;
            String offSaleKey = entry.getValue().offSaleKey;

            if (StringUtils.isBlank(onSaleKey) && StringUtils.isBlank(offSaleKey)) {
                continue;
            }

            Pattern onSalePattern = Pattern.compile(onSaleKey);
            Pattern offSalePattern = Pattern.compile(offSaleKey);

            List<ResalerProduct> products = ResalerProduct.find("deleted != ? and url is not null", DeletedStatus.DELETED).fetch();

            for (ResalerProduct product : products) {
                String url = product.url;
                //变更前的状态
                ResalerProductStatus preStatus = product.status;
                String retResponse = WebServiceRequest.url(url).getString();
                Matcher onSaleMatcher = onSalePattern.matcher(retResponse);
                Matcher offSaleMatcher = offSalePattern.matcher(retResponse);
                if (preStatus != ResalerProductStatus.ONSALE && onSaleMatcher.find()) {
                    product.status = ResalerProductStatus.ONSALE;
                } else if (preStatus != ResalerProductStatus.OFFSALE && (!onSaleMatcher.find() || offSaleMatcher.find())) {
                    product.status = ResalerProductStatus.OFFSALE;
                } else if (!offSaleMatcher.find() && !onSaleMatcher.find()) {
                    product.status = ResalerProductStatus.UNKNOWN;
                }
                product.save();
            }
        }
    }
}
