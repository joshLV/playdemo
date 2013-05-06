package models.ktv;

/**
 * KTV房间类型.
 */
public enum KtvRoomType {

    MINI("MINI", "迷你包", "123"),
    SMALL("SMALL", "小包", "456"),
    MIDDLE("MIDDLE", "中包", "789"),
    LARGE("LARGE", "大包", "198"),
    DELUXE("DELUXE", "豪华包", "432");

    private String type;
    private String name;
    private String taobaoId;

    private KtvRoomType(String type, String name, String taobaoId) {
        this.type = type;
        this.name = name;
        this.taobaoId = taobaoId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTaobaoId() {
        return taobaoId;
    }

    @Override
    public String toString(){
        return this.type;
    }
}
