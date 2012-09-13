package com.uhuila.website.cas.oauth.sinaweibo;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;

/**
 * 新浪微博Oauth api信息.
 * <p/>
 * User: sujie
 * Date: 8/30/12
 * Time: 5:03 PM
 */
public class SinaWeiboApi20 extends DefaultApi20 {
    private static final String AUTHORIZE_URL = "https://api.weibo" +
            ".com/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s";

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
        return "https://api.weibo.com/oauth2/access_token";
//        return "http://api.weibo.com/oauth2/access_token?grant_type=authorization_code";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }
}