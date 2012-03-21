package org.jasig.cas.userauthentication;

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

        System.out.println("<<<<< hello >>>>>");
        String username = Credentials.getUsername();
        String password = Credentials.getPassword();

        System.out.println("================ H " + username + " ================");
        if (log.isDebugEnabled()) {
            log.debug("email=" + username);
        }
        String sql = "select * from resaler where  email= ?";
        Object[] params = new Object[] { username };

        List<Map<String, Object>> userlist = getJdbcTemplate().queryForList(sql, params);

        if (userlist.size() == 0) {
            return false;
        }
        Map<String, Object> user = userlist.get(0);

        if (!DigestUtils.md5Hex(password + user.get("password_salt")).equals(user.get("encrypted_password"))) {
            return false;
        }

        return true;
    }

}
