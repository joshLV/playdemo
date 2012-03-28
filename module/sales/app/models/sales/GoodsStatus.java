package models.sales;

/**
 * 商品状态.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 4:57 PM
 */
public enum GoodsStatus {
    //已上架
    ONSALE,
    //已下架
    OFFSALE,
    //申请上架
    APPLY,
    //拒绝上架
    REJECT,
    //预览
    UNCREATED
}
