package models.ktv;

import models.supplier.Supplier;

import java.util.List;

/**
 * KTV促销类型
 * <p/>
 * User: wangjia
 * Date: 13-4-18
 * Time: 上午11:13
 */
public enum KtvPromotionType {
    CONTINUOUS_RESERVE_DISCOUNT("连续预订打折"),  //连续预订打折
    CONTINUOUS_RESERVE_REDUCTION("连续预订立减"),   //连续预订立减
    ADVANCED_RESERVE_DISCOUNT("提前预订打折"),   //提前预订打折
    ADVANCED_RESERVE_REDUCTION("提前预订立减");  //提前预订立减

    String _name;

    KtvPromotionType(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }
}
