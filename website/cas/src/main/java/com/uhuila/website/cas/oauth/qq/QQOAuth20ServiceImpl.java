package com.uhuila.website.cas.oauth.qq;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.net.URLEncoder;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/12/12
 * Time: 3:15 PM
 */
public class QQOAuth20ServiceImpl implements OAuthService {
    private static final String VERSION = "2.0";

    private final DefaultApi20 api;
    private final OAuthConfig config;

    /**
     * Default constructor
     *
     * @param api    OAuth2.0 api information
     * @param config OAuth 2.0 configuration param object
     */
    public QQOAuth20ServiceImpl(DefaultApi20 api, OAuthConfig config) {
        this.api = api;
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
        request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
        request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
        request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, verifier.getValue());
        request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
        if (config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());

//        System.out.println("!!!!!!!!!!!!!          request:" + request);
        Response response = request.send();

//        System.out.println("!!!!!!!!!!!!!          response:" + response.getBody());
        return api.getAccessTokenExtractor().extract(response.getBody());
    }

    /**
     * {@inheritDoc}
     */
    public Token getRequestToken() {
        throw new UnsupportedOperationException("Unsupported operation, please use 'getAuthorizationUrl' and redirect your users there");
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    public void signRequest(Token accessToken, OAuthRequest request) {
        request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthorizationUrl(Token requestToken) {
        return api.getAuthorizationUrl(config);
    }


    /**
     * 为了能重新设置redirect_url为带有返回链接的地址，重写了这个方法.
     *
     * @param o
     * @param verifier
     * @param services
     * @return
     */
    public Token getAccessToken(Object o, Verifier verifier, String[] services) {
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
        request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
        request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
        request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, verifier.getValue());
        System.out.println("services[0]:" + services[0]);
        final String url = config.getCallback() + URLEncoder
                .encode("&service=" + services[0]);

        System.out.println("!!!!!!QQOAuth20ServiceImpl!!!!!!!          url:" + url);

        request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, url);
        if (config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());

        System.out.println("!!!!!!QQOAuth20ServiceImpl!!!!!!!          request:" + request);
        Response response = request.send();

        System.out.println("!!!!!!QQOAuth20ServiceImpl!!!!!!!          response:" + response.getBody());
        return api.getAccessTokenExtractor().extract(response.getBody());
    }
}
