package models.supplier;

/**
 * User: yan
 * Date: 13-8-5
 * Time: 下午4:50
 */
public enum ReceivedType {
    ONLINE_FEE("ONLINE_FEE", "上线费"),
    ADS_FEE("ADS_FEE", "广告费"),
    OPERATE_FEE("OPERATE_FEE", "代运营费");

    ReceivedType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;// 代号
    private String name;// 中文名称
    @Override
    public String toString() {
        return this.code;
    }

    public String getName() {
        return name;
    }
}
