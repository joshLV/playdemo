package models.order;

/**
 * @author likang
 *         Date: 12-9-18
 */
public enum OuterOrderPartner {
    DD("DD", "当当", "dangdang"),     // 当当
    YHD("YHD", "一号店", "yihaodian"),    // 一号店
    JD("JD", "京东", "jingdong"),     // 京东
    WB("WB", "58", "wuba"),     // 58
    TB("TB", "淘宝", "taobao"),     // 淘宝电子凭证
    SINA("SINA", "新浪", "sina");   //新浪卡券

    private String code;// 代号
    private String name;// 中文名称
    private String loginName; //登录帐号

    OuterOrderPartner(String code, String name, String loginName) {
        this.code = code;
        this.name = name;
        this.loginName = loginName;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public String partnerName() {
        return name;
    }

    public String partnerLoginName() {
        return loginName;
    }
}
