package com.uhuila.website.cas.oauth;

import org.codehaus.jackson.JsonNode;
import org.scribe.up.profile.UserProfile;

/**
 * OAuth用户信息工具类.
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 9:27 AM
 */
public class OAuthUserProfileHelper {
    public static void addAttribute(UserProfile userProfile, JsonNode json, String key, Object value) {
        userProfile.addAttribute(key, value);
    }

    public static void addIdentifier(UserProfile userProfile, JsonNode userJson, String propertyName, String source) {
        final String identifier = userJson.get(propertyName).getTextValue();
        System.out.println(">>>> OAuth Identifier:" + identifier);

        userProfile.setId(source + ":" + identifier);
        addAttribute(userProfile, userJson, "source", source);
    }
}
