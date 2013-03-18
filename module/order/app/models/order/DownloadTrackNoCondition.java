package models.order;

import models.accounts.AccountType;
import models.resale.Resaler;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载渠道运单的查询条件
 * <p/>
 * User: wangjia
 * Date: 13-3-13
 * Time: 下午6:57
 */
public class DownloadTrackNoCondition implements Serializable {

    public Date paidBeginAt;
    public Date paidEndAt;
    public Date sentBeginAt;
    public Date sentEndAt;
    public OuterOrderPartner outerOrderPartner;
    public boolean unDownloaded;

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder builder = new StringBuilder();
        if (outerOrderPartner == null) {
            outerOrderPartner = OuterOrderPartner.JD;
        }
        builder.append(" where oi.shippingInfo.expressNumber is not null" +
                " and oi.order.userType =:userType and " +
                " oi.order.userId =:resalerId and oi.outerGoodsNo is not null");
        paramMap.put("userType", AccountType.RESALER);
        paramMap.put("resalerId", Resaler.findOneByLoginName(outerOrderPartner.partnerLoginName()).id);

        if (paidBeginAt != null) {
            builder.append(" and oi.shippingInfo.paidAt >= :paidBeginAt");
            paramMap.put("paidBeginAt", paidBeginAt);
        }
        if (paidEndAt != null) {
            builder.append(" and oi.shippingInfo.paidAt <= :paidEndAt");
            paramMap.put("paidEndAt", com.uhuila.common.util.DateUtil.getEndOfDay(paidEndAt));
        }
        if (sentBeginAt != null) {
            builder.append(" and oi.sendAt >=:sentBeginAt");
            paramMap.put("sentBeginAt", sentBeginAt);
        }
        if (sentEndAt != null) {
            builder.append(" and oi.sendAt <=:sentEndAt");
            paramMap.put("sentEndAt", com.uhuila.common.util.DateUtil.getEndOfDay(sentEndAt));
        }
        if (unDownloaded) {
            builder.append(" and oi.shippingInfo.uploadedAt is not null");
        }
        return builder.toString();
    }

    public Map<String, Object> getParams() {
        return paramMap;
    }
}
