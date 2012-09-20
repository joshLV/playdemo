package com.uhuila.website.cas.oauth.renren;

import com.uhuila.website.cas.oauth.AbstractOAuth20Provider;
import com.uhuila.website.cas.oauth.OAuthUserProfileHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuthProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 人人网登录.
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 1:57 PM
 */
public class RenRenProvider extends AbstractOAuth20Provider {

    Map<String, Object> mainAttributes = new HashMap<String, Object>();

    /**
     * Create a new instance of the provider.
     *
     * @return A new instance of the provider
     */
    @Override
    protected BaseOAuthProvider newProvider() {
        return new RenRenProvider();
    }

    @Override
    protected void internalInit() {
        service = new ServiceBuilder().provider(RenRenApi20.class).apiKey(key)
                .apiSecret(secret).callback(callbackUrl).build();

        String[] names = new String[]{"uid", "name"};
        for (String name : names) {
            mainAttributes.put(name, null);
        }

    }


    @Override
    public UserProfile getUserProfile(final OAuthCredential credential) {
        init();
        final Token accessToken = getAccessToken(credential);
        final String body = sendRequestForData(accessToken, getProfileUrl(), Verb.POST);
        if (body == null) {
            return null;
        }
        final UserProfile profile = extractUserProfile(body);
        addAccessTokenToProfile(profile, accessToken);
        return profile;
    }

    private String sendRequestForData(Token accessToken, String profileUrl, Verb verb) {
//        logger.debug("accessToken : {} / dataUrl : {}", accessToken, dataUrl);
        final long t0 = System.currentTimeMillis();
        final OAuthRequest request = new OAuthRequest(verb, profileUrl);
        if (connectTimeout != 0) {
            request.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        }
        if (readTimeout != 0) {
            request.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);
        }
        service.signRequest(accessToken, request);

        final Response response = request.send();
        final int code = response.getCode();
        final String body = response.getBody();
        final long t1 = System.currentTimeMillis();
//        logger.debug("Request took : " + (t1 - t0) + " ms for : " + dataUrl);
//        logger.debug("response code : {} / response body : {}", code, body);
        if (code != 200) {
//            logger.error("Failed to get user data, code : " + code + " / body : " + body);
            return null;
        }
        return body;
    }

    /**
     * 补充profile的url.
     *
     * @param attributes
     * @return
     */
    protected String getProfileUrl(String accessToken, Map<String, String[]> attributes) {
        StringBuilder attrsBuilder = new StringBuilder();
        attrsBuilder.append("&access_token=").append(accessToken);

        return getProfileUrl() + attrsBuilder.toString();
    }

    @Override
    protected String getProfileUrl() {
        return "http://api.renren.com/restserver.do";
        //?method=users.getInfo&format=JSON
//        return "https://graph.renren.com/v2/user";
    }


    /**
     * 为了能用post方式发送请求，覆盖此方法.
     *
     * @param accessToken
     * @return
     */
    @Override
    public UserProfile getUserProfile(final Token accessToken) {
        final String body = sendRequestForData(accessToken, getProfileUrl(), Verb.POST);
        if (body == null) {
            return null;
        }
        final UserProfile profile = extractUserProfile(body);
        addAccessTokenToProfile(profile, accessToken);
        return profile;
    }

    @Override
    protected UserProfile extractOAuthUserProfile(String body) {
        System.out.println("???????????body:" + body);


        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        ArrayNode statuses = (ArrayNode) json.get("statuses");
        JsonNode userJson = statuses.get(0).get("user");
        if (userJson != null) {
            OAuthUserProfileHelper.addIdentifier(userProfile, userJson, "id", getOpenIdSource());
            for (String attribute : mainAttributes.keySet()) {
                OAuthUserProfileHelper.addAttribute(userProfile, attribute,
                        mainAttributes.get(attribute));
            }
        }
        JsonNode subJson = userJson.get("id");
        if (subJson != null) {
            OAuthUserProfileHelper
                    .addAttribute(userProfile, "uid", subJson.getIntValue());
        }
        subJson = userJson.get("domain");
        if (subJson != null) {
            OAuthUserProfileHelper.addAttribute(userProfile, "username",
                    subJson.getTextValue());
        }

        return userProfile;
    }
}
