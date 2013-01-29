package models.sales;

/**
 * @author likang
 *         Date: 13-1-26
 */
public enum  ResalerProductStatus {
    STAGING,    //暂存 上传未成功或者仅仅是保存
    UPLOADED,   //上传成功
    REJECTED,   //审核失败
    ONSALE,     //已知为上架状态
    OFFSALE,    //已知为下架状态
    UNKONWN,    //某种原因未知（欢迎在此补充原因）
}
