package models.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SentAvailableECouponInfo {
	public List<String> availableECouponSNs=new ArrayList<>();

	public BigDecimal sumFaceValue;
	
	public ECoupon lastECoupon;
}
