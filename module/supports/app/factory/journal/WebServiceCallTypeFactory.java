package factory.journal;

import models.journal.WebServiceCallType;
import factory.ModelFactory;

public class WebServiceCallTypeFactory extends ModelFactory<WebServiceCallType> {

    @Override
    public WebServiceCallType define() {
        WebServiceCallType callType = new WebServiceCallType();
        callType.callType = "test";
        callType.description = "万能调用卡"; // for test name
        callType.key1Name = "订单号";
        callType.key2Name = "名称";
        callType.key3Name = "电话号码";
        return callType;
    }

}
