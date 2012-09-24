package com.uhuila.website.cas.oauth.renren;

import com.uhuila.website.cas.oauth.AbstractOAuth20Provider;
import com.uhuila.website.cas.oauth.OAuthUserProfileHelper;
import org.codehaus.jackson.JsonNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuthProvider;

import java.util.HashMap;
import java.util.Map;

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

        String[] names = new String[]{"name"};
        for (String name : names) {
            mainAttributes.put(name, null);
        }

    }

    @Override
    protected String getProfileUrl() {
        return "http://api.renren.com/restserver.do";
    }

    /**
     * 为了能用post方式发送请求，覆盖此方法.
     *
     * @param accessToken
     * @return
     */
    @Override
    public UserProfile getUserProfile(final Token accessToken) {
        final String body = accessToken.getRawResponse();
        if (body == null) {
            return null;
        }
        final UserProfile profile = extractUserProfile(body);
        addAccessTokenToProfile(profile, accessToken);
        return profile;
    }

    @Override
    protected UserProfile extractOAuthUserProfile(String body) {
//        System.out.println("???????????body:" + body);


        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        JsonNode userJson = json.get("user");
        if (userJson != null) {
            JsonNode subJson = userJson.get("id");
            if (subJson != null) {
                OAuthUserProfileHelper.addIdentifier(userProfile, String.valueOf(subJson.getIntValue()), getOpenIdSource());
                for (String attribute : mainAttributes.keySet()) {
                    OAuthUserProfileHelper.addAttribute(userProfile, attribute,
                            mainAttributes.get(attribute));
                }
                OAuthUserProfileHelper
                        .addAttribute(userProfile, "uid", subJson.getIntValue());
            }
        }

        return userProfile;
    }
}
