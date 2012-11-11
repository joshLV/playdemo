package util.ws;

import play.Play;

public class WebServiceClientFactory {

    public static WebServiceClient getClientHelper() {
        if (Play.runingInTestMode()) {
            return MockWebServiceClient.getInstance();
        }
        return PlayWebServiceClient.getInstance();
    }
}
