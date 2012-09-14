package models.yihaodian.groupbuy.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class YHDErrorResponse {
    public int errorCount;
    public List<YHDErrorInfo> errInfoList;
    public YHDErrorResponse(){
        errorCount = 0;
        errInfoList = new ArrayList<>();
    }

    public void addErrorInfo(YHDErrorInfo errorInfo) {
        errorCount += 1;
        errInfoList.add(errorInfo);
    }
}
