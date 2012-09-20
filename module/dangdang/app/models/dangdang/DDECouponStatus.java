package models.dangdang;

/**
 * 码的状态，0：未使用；1：已使用；2：已过期；30：已退款；40：已经使用后的退款。.
 * <p/>
 * User: sujie
 * Date: 9/17/12
 * Time: 1:20 PM
 */
public enum DDECouponStatus {
    UNUSED(0), USED(1), EXPIRED(2), REFUNDED(30), USE_REFUNDED(40);

    private int status;


    DDECouponStatus(int status) {
        this.status = status;
    }


    public static DDECouponStatus getStatus(int status) {
        switch (status) {
            case 0:
                return UNUSED;
            case 1:
                return USED;
            case 2:
                return EXPIRED;
            case 30:
                return REFUNDED;
            case 40:
                return USE_REFUNDED;
            default:
                return UNUSED;
        }
    }
}
