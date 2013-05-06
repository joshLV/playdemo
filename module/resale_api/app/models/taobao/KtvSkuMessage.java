package models.taobao;

import models.ktv.KtvPriceSchedule;
import models.sales.Shop;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.Map;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:27
 */
public class KtvSkuMessage implements Serializable {
    private static final long serialVersionUID = -8179923251882104351L;

    public String partnerProductId;

    private Map<String, Object> params;

    public String getPartnerProductId() {
        return partnerProductId;
    }

    public void setPartnerProductId(String partnerProductId) {
        this.partnerProductId = partnerProductId;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public void putParam(String key, Object obj) {
        this.params.put(key, obj);
    }

    public KtvSkuMessage(String partnerProductId, Map<String, Object> params) {
        this.partnerProductId = partnerProductId;
        this.params = params;
    }
}
