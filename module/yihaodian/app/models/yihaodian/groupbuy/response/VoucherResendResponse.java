package models.yihaodian.groupbuy.response;

/**
 * @author likang
 *         Date: 12-9-14
 */
public class VoucherResendResponse {
    public int errorCount;
    public int totalCount;
    public VoucherResendResponse(){
        errorCount = 0;
        totalCount = 0;
    }
    public VoucherResendResponse(int errorCount, int totalCount){
        this.errorCount = errorCount;
        this.totalCount = totalCount;
    }
}
