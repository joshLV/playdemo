package com.uhuila.website.cas.sinaweibo;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuth20Provider;
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
public class SinaWeiboProvider extends BaseOAuth20Provider {


//    AttributePrincipal principal = assertion.getPrincipal();
//    String id = principal.getName();
    Map<String, Object> mainAttributes = new HashMap<String, Object>();
//    ()principal.getAttributes();

    /**
     * Create a new instance of the provider.
     *
     * @return A new instance of the provider
     */
    @Override
    protected BaseOAuthProvider newProvider() {
        return this;
    }

    @Override
    protected void internalInit() {
//        if (scope != null) {
//            service = new ServiceBuilder().provider(SinaWeiboApi20.class).apiKey(key)
//                    .apiSecret(secret).callback(callbackUrl).scope(scope).build();
//        } else {
        service = new ServiceBuilder().provider(SinaWeiboApi20.class).apiKey(key)
                .apiSecret(secret).callback(callbackUrl).build();
//        }
        String[] names = new String[]{"uid", "username"};
        for (String name : names) {
            mainAttributes.put(name, null);
        }

    }

    @Override
    protected String getProfileUrl() {
        return "https://api.weibo.com/2/statuses/user_timeline.json";
    }

    @Override
    protected UserProfile extractUserProfile(String body) {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        ArrayNode statuses = (ArrayNode) json.get("statuses");
        JsonNode userJson = statuses.get(0).get("user");
        if (json != null) {
            UserProfileHelper.addIdentifier(userProfile, userJson, "id");
            for (String attribute : mainAttributes.keySet()) {
                UserProfileHelper.addAttribute(userProfile, json, attribute,
                        mainAttributes.get(attribute));
            }
        }
        JsonNode subJson = userJson.get("id");
        if (subJson != null) {
            UserProfileHelper
                    .addAttribute(userProfile, json, "uid", subJson.getIntValue());

        }
        subJson = userJson.get("domain");
        if (subJson != null) {
            UserProfileHelper.addAttribute(userProfile, json, "username",
                    subJson.getTextValue());
        }

        return userProfile;
    }


}
class UserProfileHelper {
    static void addAttribute(UserProfile userProfile, JsonNode json, String key, Object value){
          userProfile.addAttribute(key,value);
    }

    public static void addIdentifier(UserProfile userProfile, JsonNode userJson, String id) {
        userProfile.setId(userJson);
    }
}

