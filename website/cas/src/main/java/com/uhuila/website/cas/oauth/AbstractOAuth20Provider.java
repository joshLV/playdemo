package com.uhuila.website.cas.oauth;

import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuth20Provider;
import org.scribe.up.session.UserSession;
import org.scribe.utils.OAuthEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URLDecoder;
import java.util.Map;


/**
 * OAuth2.0协议中利用state状态记录应用的初始来源页面.
 * <p/>
 * alter table users add(open_id varchar(255),open_id_source varchar(255));
 * <p/>
 * User: sujie
 * Date: 9/11/12
 * Time: 9:29 AM
 */
public abstract class AbstractOAuth20Provider extends BaseOAuth20Provider {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractOAuth20Provider.class);

    /**
     * 在使用oauth协议登录前设置应用的初始来源页面.
     *
     * @param session
     * @return
     */
    @Override
    public String getAuthorizationUrl(UserSession session) {
        init();
        // no requestToken for OAuth 2.0 -> no need to save it in the user session
        String authorizationUrl = service.getAuthorizationUrl(null);


        final String url = authorizationUrl + "&state=" + session.getAttribute(OAuthConstants.SERVICE);
        logger.debug("!!!!AbstractOAuth20Provider!!!!    getAuthorizationUrl:" + url);

        return url;
    }


    /**
     * 从state参数中读取登录前应用的初始来源页面地址，重新设置service对象.
     *
     * @param session
     * @param parameters
     * @return
     */
    @Override
    public OAuthCredential extractCredentialFromParameters(UserSession session, Map<String, String[]> parameters) {
        String[] state = parameters.get("state");
        /*for (String key : parameters.keySet()) {
            System.out.println("??????????    " + key + ":" + parameters.get(key)[0]);
        }
        System.out.println("????service=" + service);*/
        if (state == null) {
            state = parameters.get("service");
        }
        String service = state == null ? null : state[0];

        session.setAttribute("service", new SimpleWebApplicationServiceImpl(URLDecoder.decode(service)));
        String[] verifiers = parameters.get(OAUTH_CODE);
        if (verifiers != null && verifiers.length == 1) {
            String verifier = OAuthEncoder.decode(verifiers[0]);
            logger.debug("verifier : {}", verifier);
            return new OAuthCredential(null, null, verifier, getType(), parameters);
        } else {
            logger.debug("No credential found");
            return null;
        }
    }

    @Override
    protected UserProfile extractUserProfile(String body) {
        UserProfile profile = extractOAuthUserProfile(body);
        if (profile != null) {
            addLocalUser(profile);
        }
        return profile;
    }

    protected abstract UserProfile extractOAuthUserProfile(String body);

    protected void addLocalUser(UserProfile userProfile) {
        final User user = new User();
        user.openId = userProfile.getAttributes().get("uid").toString();
        user.openIdSource = userProfile.getAttributes().get("source").toString();

        if (user.openIdSource.equalsIgnoreCase("0")) {
            new Exception().printStackTrace();
        }

        //创建用户到数据库中，但先确保该用户不存在.
        final String getUserSql = "select count(*) from users where open_id='" + user.openId + "' and " +
                "open_id_source='" + user.openIdSource + "\'";
        int count = getJdbcTemplate().queryForInt(getUserSql);
        if (count <= 0) {
            final String sql = "insert into users(created_at,open_id_source,open_id,status) values(current_timestamp,'" +
                    user.openIdSource + "','" + user.openId + "','NORMAL')";
            logger.debug("sql:" + sql);
            getJdbcTemplate().update(sql);
        }
//        getJdbcTemplate().update(sql, new PreparedStatementSetter() {
//            public void setValues(PreparedStatement ps) throws SQLException {
//                System.out.println(">>>>>>>>Prepare to insert to users:  user.openIdSource:" + user.openIdSource);
//                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
////                ps.setString(2, user.openIdSource);
////                ps.setString(2, user.openId);
//            }
//        });
    }

    protected String getOpenIdSource() {
        final int typeEndIndex = getType().indexOf("Provider");
        if (typeEndIndex < 0) {
            return getType();
        }
        return getType().substring(0, typeEndIndex);
    }

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private class User {
        String openId;
        String openIdSource;
    }
}
