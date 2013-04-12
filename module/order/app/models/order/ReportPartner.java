package models.order;

public enum ReportPartner {
    DD("DD", "当当", "dangdang"),     // 当当
    YHD("YHD", "一号店", "yihaodian"),    // 一号店
    JD("JD", "京东", "jingdong"),     // 京东
    WB("WB", "58同城", "wuba"),     // 58
    TB("TB", "聚划算", "taobao"),     // 淘宝电子凭证
    YBQ("YBQ", "一百券", "yibaiquan");

    private String code;// 代号
    private String name;// 中文名称
    public String loginName; //登录帐号

    ReportPartner(String code, String name, String loginName) {
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
    public String codeName(){
        return code;
    }
}
