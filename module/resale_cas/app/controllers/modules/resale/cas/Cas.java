package controllers.modules.resale.cas;

import java.io.Serializable;
import models.resale.Resaler;

/**
 * TODO: controllers.modules.resale.cas.Cas这个类的具体职责是？能否修改一个名字？
 * <p/>
 * User: sujie
 * Date: 2/22/12
 * Time: 11:21 AM
 */
public class Cas implements Serializable {
    public boolean isLogin = false;

    public String username;
    
    public Resaler resaler;
}
