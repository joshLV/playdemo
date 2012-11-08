package util.http;

import play.libs.WS;

public class DefaultHttpClientHelper implements HttpClientHelper {

    private static DefaultHttpClientHelper _instance;
    
    public static DefaultHttpClientHelper getInstance() {
        if (_instance == null) {
            _instance = new DefaultHttpClientHelper();
        }
        return _instance;
    }
    
    private DefaultHttpClientHelper() {}
    
    @Override
    public void processGetUrl(String url, HttpCallback callback) {

        play.libs.WS.HttpResponse response = WS.url(url).get();
        
        callback.process(response.getStatus(), response.getString());

    }

}
