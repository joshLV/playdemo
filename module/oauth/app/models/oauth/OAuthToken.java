package models.oauth;

import java.util.Date;

import javax.persistence.*;

import models.accounts.AccountType;
import play.db.jpa.Model;

@Entity
@Table(name="oauth_token")
public class OAuthToken extends Model{
    @Column(name = "user_id")
    public Long  userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;

    @Column(name = "identity")
    public String identity;

    @Enumerated(EnumType.STRING)
    @Column(name = "web_site")
    public WebSite webSite;

    @Column(name = "service_user_id")
    public String serviceUserId;

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

    public boolean isExpired(){
        return this.accessTokenExpiresAt.after(new Date());
    }

    public static OAuthToken getOAuthToken(String serviceUserId, WebSite webSite){
        return OAuthToken.find("byWebSiteAndServiceUserId", webSite, serviceUserId).first();
    }

    public static OAuthToken getOAuthToken(Long userId, AccountType accountType, WebSite webSite){
        return OAuthToken.find("byIdentityAndWebSite", accountType+"_" + userId, webSite).first();
//        return OAuthToken.find("byUserIdAndAccountTypeAndWebSite", userId, accountType, webSite).first();
//        if(token != null && token.isExpired()){
//            token.delete();
//            return null;
//        }
//        return token;
    }

}
