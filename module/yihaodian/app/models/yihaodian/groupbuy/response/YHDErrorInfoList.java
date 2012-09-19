package models.yihaodian.groupbuy.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-19
 */
public class YHDErrorInfoList {
    public List<YHDErrorInfo> errDetailInfo;
    public YHDErrorInfoList(){
        errDetailInfo = new ArrayList<>();
    }

    public void addError(YHDErrorInfo errorInfo){
        errDetailInfo.add(errorInfo);
    }
}
