package factory.journal;

import models.journal.WebServiceCallLog;
import factory.ModelFactory;

public class WebServiceCallLogFactory extends ModelFactory<WebServiceCallLog> {

    @Override
    public WebServiceCallLog define() {
        WebServiceCallLog log = new WebServiceCallLog();
        log.callType = "test";
        log.callMethod = "GET";
        log.url = "http://webserver.foo.com/doo";
        log.responseText = "SUCCESS";
        log.key1 = "123";
        log.key2 = "abc";
        log.key3 = "987";
        log.statusCode = 200;
        log.duration = 2l;
        return log;
    }

}
