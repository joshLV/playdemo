package com.uhuila.suppliers.cas;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BlockedCredentialsAuthenticationException;
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

        String sql = "select a.*, b.status as supplier_status from supplier_users a inner join suppliers b on a.supplier_id=b.id where a.login_name = ? " +
                "and b.domain_name = ? and a.deleted=0 and b.deleted=0";
        Object[] params = new Object[] { loginName, domainName };

        List<Map<String, Object>> userlist = getJdbcTemplate().queryForList(sql, params);

        if (userlist.size() == 0) {
            return false;
        }
        Map<String, Object> user = userlist.get(0);

        if (!"NORMAL".equals(user.get("supplier_status"))) {
            throw new BlockedCredentialsAuthenticationException();
        }
        
        if (!DigestUtils.md5Hex(password + user.get("password_salt")).equals(user.get("encrypted_password"))) {
            return false;
        }
        
        // 登录成功，记录一下登录的时间
        String timeSql = "update supplier_users set last_login_at=? where id=?";
        getJdbcTemplate().update(timeSql, new Date(), user.get("id"));

        return true;
    }


}
