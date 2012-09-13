package models.yihaodian.groupbuy.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class VoucherInfoResponse {
    private int errorCount;
    private int totalCount;
    private VoucherInfoList voucherInfoList;

    public VoucherInfoResponse(){
        errorCount = 0;
        totalCount = 0;
        voucherInfoList = new VoucherInfoList();
    }

    public void add(VoucherInfo voucherInfo) {
        totalCount += 1;
        voucherInfoList.addVoucher(voucherInfo);
    }
}