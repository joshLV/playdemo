package controllers.modules.website.cas;

import models.consumer.User;

import java.io.Serializable;

/**
 * TODO: controllers.modules.website.cas.Cas这个类的具体职责是？能否修改一个名字？
 * <p/>
 * User: sujie
 * Date: 2/22/12
 * Time: 11:21 AM
 */
public class Cas implements Serializable {
    public boolean isLogin = false;

    public String loginName;
    
    public User user;
}
