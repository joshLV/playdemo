package models.order;

import java.math.BigDecimal;
import java.util.List;

public class SentAvailableECouponInfo {
	public List<String> availableECouponSNs;

	public BigDecimal sumFaceValue;
	
	public ECoupon lastECoupon;
}
