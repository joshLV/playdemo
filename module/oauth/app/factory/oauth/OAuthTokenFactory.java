package factory.oauth;

import factory.ModelFactory;
import models.oauth.OAuthToken;

/**
 * @author likang
 *         Date: 13-5-25
 */
public class OAuthTokenFactory extends ModelFactory<OAuthToken> {

        @Override
        public OAuthToken define() {
            OAuthToken token = new OAuthToken();
            return token;
        }
}
