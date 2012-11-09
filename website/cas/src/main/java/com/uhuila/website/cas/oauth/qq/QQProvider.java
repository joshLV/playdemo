package com.uhuila.website.cas.oauth.qq;

import com.uhuila.website.cas.oauth.AbstractOAuth20Provider;
import com.uhuila.website.cas.oauth.OAuthUserProfileHelper;
import org.scribe.builder.ServiceBuilder;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuthProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * QQ登录.
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 1:56 PM
 */
public class QQProvider extends AbstractOAuth20Provider {

    Map<String, Object> mainAttributes = new HashMap<String, Object>();

    /**
     * Create a new instance of the provider.
     *
     * @return A new instance of the provider
     */
    @Override
    protected BaseOAuthProvider newProvider() {
        return new QQProvider();
    }

    @Override
    protected void internalInit() {
        service = new ServiceBuilder().provider(QQApi20.class).apiKey(key)
                .apiSecret(secret).callback(callbackUrl).build();

        String[] names = new String[]{"id", "name"};
        for (String name : names) {
            mainAttributes.put(name, null);
        }

    }


    //
//    /**
//     * 在使用oauth协议登录前设置应用的初始来源页面.
//     *
//     * @param session
//     * @return
//     */
//    @Override
//    public String getAuthorizationUrl(UserSession session) {
//        init();
//        // no requestToken for OAuth 2.0 -> no need to save it in the user session
//        String authorizationUrl = service.getAuthorizationUrl(null);
//
//        String serviceParam = authorizationUrl.contains("redirect_url") ? URLEncoder.encode(
//                "?service=" + session.getAttribute(OAuthConstants.SERVICE)) : URLEncoder.encode(
//                "&service=" + session.getAttribute(OAuthConstants.SERVICE));
//        final String url = authorizationUrl + serviceParam + "&state=" + session.getAttribute(OAuthConstants.SERVICE);
//        System.out.println("!!!!AbstractOAuth20Provider!!!!    getAuthorizationUrl:" + url);
//
//        return url;
//    }
//
//    @Override
//    public UserProfile getUserProfile(final OAuthCredential credential) {
//        init();
//        final Token accessToken = getAccessToken(credential);
//        final String body = sendRequestForData(accessToken, getProfileUrl(accessToken.getToken(), credential.getAttributes()));
//        if (body == null) {
//            return null;
//        }
//        final UserProfile profile = extractUserProfile(body);
//        addAccessTokenToProfile(profile, accessToken);
//        return profile;
//    }
//
//    @Override
//    public Token getAccessToken(OAuthCredential credential) {
//        // no request token saved in user session and no token (OAuth v2.0)
//
//        String verifier = credential.getVerifier();
//        Verifier providerVerifier = new Verifier(verifier);
//
//        Token accessToken;
//        if (service instanceof QQOAuth20ServiceImpl) {
//            QQOAuth20ServiceImpl serviceImpl = (QQOAuth20ServiceImpl) service;
//            accessToken = serviceImpl.getAccessToken(null, providerVerifier, credential.getAttributes().get("service"));
//        } else {
//            accessToken = service.getAccessToken(null, providerVerifier);
//        }
//
//        return accessToken;
//    }
//
//
//    /**
//     * 补充profile的url.
//     *
//     * @param attributes
//     * @return
//     */
//    protected String getProfileUrl(String accessToken, Map<String, String[]> attributes) {
//        StringBuilder attrsBuilder = new StringBuilder();
//        attrsBuilder.append("&access_token=").append(accessToken);
//        attrsBuilder.append("&openid=").
//                append(attributes.get("openid")[0]);
//
//        return getProfileUrl() + attrsBuilder.toString();
//    }
//
    @Override
    protected String getProfileUrl() {
        return "https://graph.qq.com/oauth2.0/me";
//        return "http://open.t.qq.com/api/user/info?format=json&oauth_consumer_key=100659104&oauth_version=2.a";
    }

    @Override
    protected UserProfile extractOAuthUserProfile(String body) {
        UserProfile userProfile = new UserProfile();
        int idIndex = body.indexOf("\"openid\":\"") + 10;
        String id = body.substring(idIndex, body.lastIndexOf("\"}"));
//        System.out.println("id:" + id);
        OAuthUserProfileHelper.addIdentifier(userProfile, id, getOpenIdSource());
        OAuthUserProfileHelper.addAttribute(userProfile, "uid", id);

//        JsonNode userJson = json.get("data");
//        if (userJson != null) {
//            OAuthUserProfileHelper.addIdentifier(userProfile, userJson, "nick", getOpenIdSource());
//            for (String attribute : mainAttributes.keySet()) {
//                OAuthUserProfileHelper.addAttribute(userProfile, userJson, attribute,
//                        mainAttributes.get(attribute));
//            }
//        }
//        JsonNode subJson = userJson.get("nick");
//        if (subJson != null) {
//            OAuthUserProfileHelper
//                    .addAttribute(userProfile, json, "uid", subJson.getTextValue());
//        }
//        subJson = userJson.get("domain");
//        if (subJson != null) {
//            OAuthUserProfileHelper.addAttribute(userProfile, json, "username",
//                    subJson.getTextValue());
//        }

        return userProfile;
    }
}
