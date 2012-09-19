package com.uhuila.website.cas.oauth.sinaweibo;

import com.uhuila.website.cas.oauth.AbstractOAuth20Provider;
import com.uhuila.website.cas.oauth.OAuthUserProfileHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuthProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 新浪微博登录.
 * <p/>
 * User: sujie
 * Date: 8/30/12
 * Time: 5:06 PM
 */
public class SinaWeiboProvider extends AbstractOAuth20Provider {

    Map<String, Object> mainAttributes = new HashMap<String, Object>();

    /**
     * Create a new instance of the provider.
     *
     * @return A new instance of the provider
     */
    @Override
    protected BaseOAuthProvider newProvider() {
        return new SinaWeiboProvider();
    }

    @Override
    protected void internalInit() {
        service = new ServiceBuilder().provider(SinaWeiboApi20.class).apiKey(key)
                .apiSecret(secret).callback(callbackUrl).build();

        String[] names = new String[]{"id", "name", "screen_name", "city", "province", "gender"};
        for (String name : names) {
            mainAttributes.put(name, null);
        }

    }

    @Override
    protected String getProfileUrl() {
        return "https://api.weibo.com/2/statuses/user_timeline.json?count=1";
    }

    @Override
    protected UserProfile extractOAuthUserProfile(String body) {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        ArrayNode statuses = (ArrayNode) json.get("statuses");
        JsonNode userJson = statuses.get(0).get("user");
        if (userJson != null) {
            OAuthUserProfileHelper.addIdentifier(userProfile, userJson, "idstr", getOpenIdSource());
            for (String attribute : mainAttributes.keySet()) {
                OAuthUserProfileHelper.addAttribute(userProfile, userJson, attribute,
                        mainAttributes.get(attribute));
            }
        }
        JsonNode subJson = userJson.get("id");
        if (subJson != null) {
            OAuthUserProfileHelper
                    .addAttribute(userProfile, json, "uid", subJson.getIntValue());
        }
        subJson = userJson.get("domain");
        if (subJson != null) {
            OAuthUserProfileHelper.addAttribute(userProfile, json, "username",
                    subJson.getTextValue());
        }

        return userProfile;
    }
//
//    @Override
//    public String getType(){
//        return "SinaWeiboProvider";
//    }
}
