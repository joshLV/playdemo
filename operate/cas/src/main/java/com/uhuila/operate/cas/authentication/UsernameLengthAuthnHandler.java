package com.uhuila.operate.cas.authentication;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UsernameLengthAuthnHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(UsernameLengthAuthnHandler.class);

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials Credentials)
            throws AuthenticationException {

        String username = Credentials.getUsername();
        String password = Credentials.getPassword();

        System.out.println("================  " + username + " ================");
        if (log.isDebugEnabled()) {
            log.debug("email=" + username);
        }
        String sql = "select * from operate_users where login_name= ? and deleted=0";
        Object[] params = new Object[] { username };

        List<Map<String, Object>> userlist = getJdbcTemplate().queryForList(sql, params);

        if (userlist.size() == 0) {
            return false;
        }
        Map<String, Object> user = userlist.get(0);

        if (!DigestUtils.md5Hex(password + user.get("password_salt")).equals(user.get("encrypted_password"))) {
            return false;
        }

        // 登录成功，记录一下登录的时间
        String timeSql = "update operate_users set last_login_at=? where id=?";
        getJdbcTemplate().update(timeSql, new Date(), user.get("id"));

        return true;
    }

}
