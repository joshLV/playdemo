package util.ws;

import models.journal.WebServiceCallLog;
import play.libs.WS;

public class PlayWebServiceClientHelper extends WebServiceClientHelper {

    private static PlayWebServiceClientHelper _instance;
    
    public static PlayWebServiceClientHelper getInstance() {
        if (_instance == null) {
            _instance = new PlayWebServiceClientHelper();
        }
        return _instance;
    }
    
    private PlayWebServiceClientHelper() {}
    
    @Override
    public void doGet(WebServiceCallLog log, WebServiceCallback callback) {
        play.libs.WS.HttpResponse response = WS.url(log.url).get();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        callback.process(response.getStatus(), response.getString());
    }

    public void doPost(WebServiceCallLog log, WebServiceCallback callback) {
        
        WS.url("").post();
    }
}
