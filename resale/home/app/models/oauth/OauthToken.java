package models.oauth;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="oauth_token")
public class OauthToken extends Model{
    @Column(name = "user_id")
    public Long userId;
    
    @Column(name = "access_token")
    public String accessToken;
    
    @Column(name = "access_token_expires_at")
    public Date accessTokenExpiresAt;

    @Column(name = "refresh_token")
    public String refreshToken;

    @Column(name = "refresh_token_expires_at")
    public Date refreshTokenExpiresAt;
    
    
    @Column(name = "extra")
    public String extra;
    
}
