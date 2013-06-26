package jobs.goods;

import com.uhuila.common.constants.DeletedStatus;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.On;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 每小时查询各渠道商品的状态
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@JobDefine(title="渠道上下架报表", description="每小时查询各渠道商品的状态")
@On("0 0 1 * * ?")  //每天凌晨执行
public class ScannerResalerProductStatusJob extends JobWithHistory {

    @Override
    public void doJobWithHistory() {
        Map<OuterOrderPartner, Resaler> resalers = new HashMap<>();
        resalers.put(OuterOrderPartner.YHD, Resaler.findApprovedByLoginName(Resaler.YHD_LOGIN_NAME));
        resalers.put(OuterOrderPartner.WB, Resaler.findApprovedByLoginName(Resaler.WUBA_LOGIN_NAME));
        resalers.put(OuterOrderPartner.TB, Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME));
        resalers.put(OuterOrderPartner.JD, Resaler.findApprovedByLoginName(Resaler.JD_LOGIN_NAME));

        for (Map.Entry<OuterOrderPartner, Resaler> entry : resalers.entrySet()) {
            if (entry.getValue() == null) {
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
                boolean onSale = onSalePattern.matcher(retResponse).find();
                boolean offSale = offSalePattern.matcher(retResponse).find();

                if (onSale) {
                    product.status = ResalerProductStatus.ONSALE;
                } else if (offSale) {
                    product.status = ResalerProductStatus.OFFSALE;
                } else {
                    product.status = ResalerProductStatus.UNKNOWN;
                }
                product.save();
            }
        }
    }
}
