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

    public Long getScheduledId() {
        return scheduledId;
    }

    public void setScheduledId(Long scheduledId) {
        this.scheduledId = scheduledId;
    }

    /**
     * 价格策略ID
     */
    public Long scheduledId;

    public KtvSkuMessage(Long scheduledId) {
        this.scheduledId = scheduledId;
    }
}
