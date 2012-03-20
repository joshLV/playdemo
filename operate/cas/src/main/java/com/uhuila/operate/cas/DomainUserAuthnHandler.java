package com.uhuila.operate.cas;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DomainUserAuthnHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(DomainUserAuthnHandler.class);

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

        String fullUserName = Credentials.getUsername();
        String password = Credentials.getPassword();

        if (log.isDebugEnabled()) {
            log.debug("email=" + fullUserName);
        }
        
        String[] domainUsers = DomainNameUtil.getDomainUser(fullUserName);
        if (domainUsers == null) {
            return false;
        }
        
        String loginName = domainUsers[0];
        String domainName = domainUsers[1];
        
        String sql = "select a.* from cusers a inner join companies b on a.company_id=b.id where a.login_name = ? and b.domain_name = ?";
        Object[] params = new Object[] { loginName, domainName };

        List<Map<String, Object>> userlist = getJdbcTemplate().queryForList(sql, params);

        if (userlist.size() == 0) {
            return false;
        }
        Map<String, Object> user = userlist.get(0);

        if (!DigestUtils.md5Hex(password + user.get("password_salt")).equals(user.get("crypted_password"))) {
            return false;
        }

        return true;
    }


}
