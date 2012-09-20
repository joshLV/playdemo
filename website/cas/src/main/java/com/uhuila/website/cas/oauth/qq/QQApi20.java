package com.uhuila.website.cas.oauth.qq;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 1:57 PM
 */
public class QQApi20 extends DefaultApi20 {
    private static final String AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize?client_id=%s&response_type=code&redirect_uri=%s";
//    "https://open.t.qq" +
//            ".com/cgi-bin/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s";
/*
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
*/

    /**
     * Returns the access token extractor.
     * 返回的是json格式的.
     *
     * @return access token extractor
     */
//    @Override
//    public AccessTokenExtractor getAccessTokenExtractor() {
//        return new JsonTokenExtractor();
//    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code";
//        return "https://open.t.qq.com/cgi-bin/oauth2/access_token?grant_type=authorization_code";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }

    @Override
    public OAuthService createService(OAuthConfig config)
    {
        return new QQOAuth20ServiceImpl(this, config);
    }

}