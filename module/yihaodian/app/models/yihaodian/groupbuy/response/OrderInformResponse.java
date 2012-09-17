package models.yihaodian.groupbuy.response;

/**
 * @author likang
 *         Date: 12-9-14
 */
public class OrderInformResponse {
    public int errorCount;
    public int updateCount;
    public OrderInformResponse(){
        errorCount = 0;
        updateCount = 0;
    }
    public OrderInformResponse(int errorCount, int updateCount){
        this.errorCount = errorCount;
        this.updateCount = updateCount;
    }
}
