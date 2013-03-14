package models.order;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-3-13
 * Time: 下午6:57
 */
public class DownloadTrackNoCondition implements Serializable {

    public Date paidBeginAt;
    public Date paidEndAt;
    public Date sentBeginAt;
    public Date sentEndAt;
    public OuterOrderPartner outerOrderPartner;


    private Map<String, Object> params = new HashMap<>();

//    public String getFilter() {
//        StringBuilder sql = new StringBuilder();
//
//
//        return filter.toString();
//    }

    public Map<String, Object> getParams() {
        return params;
    }
}
