package org.jasig.cas.userauthentication;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.jdbc.core.JdbcTemplate;

public class UsernameLengthAuthnHandler extends AbstractUsernamePasswordAuthenticationHandler {

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
        
        String sql = "select * from users where  email= ?";
        Object[] params = new Object[]{username}; 

        List userlist = getJdbcTemplate().queryForList(sql,params);
       
        if(userlist.size()  == 0){
            return false;
        }
        Map<String,Object>   user  = (Map<String, Object>) userlist.get(0);
        
        
        if(!DigestUtils.md5Hex(password+user.get("password_salt")).equals(user.get("crypted_password"))){
            return false;
        }
        
        
        
        return true;
    }

}
