package models.yihaodian.groupbuy.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-19
 */
public class VoucherInfoList {
    private List<VoucherInfo> voucherInfo;
    public VoucherInfoList(){
        voucherInfo = new ArrayList<>();
    }
    public void addVoucher(VoucherInfo voucher){
        voucherInfo.add(voucher);
    }
}
