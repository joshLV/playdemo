package models.yihaodian.groupbuy.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class YHDErrorResponse {
    public int errorCount;
    public int totalCount;
    public YHDErrorInfoList errInfoList;
    public YHDErrorResponse(){
        errorCount = 0;
        totalCount = 0;
        errInfoList = new YHDErrorInfoList();
    }

    public void addErrorInfo(YHDErrorInfo errorInfo) {
        errorCount += 1;
        errInfoList.addError(errorInfo);
    }
}
