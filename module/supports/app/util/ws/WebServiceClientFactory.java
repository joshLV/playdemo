package util.ws;

import play.Play;

public class WebServiceClientFactory {

    public static WebServiceClient getClientHelper() {
        if (Play.runingInTestMode()) {
            return MockWebServiceClient.getInstance();
        }
        return PlayWebServiceClient.getInstance();
    }


    public static WebServiceClient getClientHelper(String encoding) {
        if (Play.runingInTestMode()) {
            return MockWebServiceClient.getInstance(encoding);
        }
        return PlayWebServiceClient.getInstance(encoding);
    }
}
