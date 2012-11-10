package util.ws;

import play.Play;

public class WebServiceClientFactory {

    public static WebServiceClientHelper getClientHelper() {
        if (Play.runingInTestMode()) {
            return MockWebServiceClientHelper.getInstance();
        }
        return PlayWebServiceClientHelper.getInstance();
    }
}
