package models.order;

/**
 * 开票方式
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午3:07
 */
public enum InvoiceType {
    PLAIN,  //普票
    VAT,    //增票
    VAT_PLAIN,  //增值税普通发票
    NO          //无发票
}