package models.ktv;

/**
 * KTV房间类型.
 */
public enum KtvRoomType {

    MINI("MINI", "迷你包", "27426219:6312905"),
    SMALL("SMALL", "小包", "27426219:3442354"),
    MIDDLE("MIDDLE", "中包", "27426219:6769368"),
    LARGE("LARGE", "大包", "27426219:3374388"),
    DELUXE("DELUXE", "豪华包", "27426219:40867986");

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

    public static KtvRoomType getRoomTypeByTaobaoId(String taobaoId)  {
        for (KtvRoomType roomType : KtvRoomType.values()) {
            if (roomType.getTaobaoId().equals(taobaoId)) {
                return roomType;
            }
        }
        return null;
    }


    @Override
    public String toString(){
        return this.type;
    }
}
