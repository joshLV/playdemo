package models.order;

/**
 * @author likang
 *         Date: 12-9-18
 */
public enum OuterOrderPartner {
    DD,     // 当当
    YHD,    // 一号店
    JD,     // 京东
    WB,     // 58
    TB;     // 淘宝电子凭证
    //添加条目的话要在下面两个方法中也设置一下


    public String partnerName() {
        switch (this){
            case DD: return "当当";
            case YHD: return "一号店";
            case JD: return "京东";
            case WB: return "58";
            case TB: return "淘宝";
            default:
                return this.toString();
        }
    }

    public String partnerLoginName() {
        switch (this){
            case DD: return "dangdang";
            case YHD: return "yihaodian";
            case JD: return "jingdong";
            case WB: return "wuba";
            case TB: return "taobao";
            default:
                return null;
        }
    }
}
