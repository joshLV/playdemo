package com.uhuila.website.cas.oauth.renren;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 1:58 PM
 */
public class RenRenApi20 extends DefaultApi20 {
    private static final String AUTHORIZE_URL = "https://graph.renren.com/oauth/authorize?" +
            "client_id=%s&response_type=code&redirect_uri=%s";
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }


    /**
     * Returns the access token extractor.
     * 返回的是json格式的.
     *
     * @return access token extractor
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }


    @Override
    public String getAccessTokenEndpoint() {
        return "https://graph.renren.com/oauth/token?grant_type=authorization_code";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }
}