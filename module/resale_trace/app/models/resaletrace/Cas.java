package models.resalecas;

import models.resale.Resaler;

import java.io.Serializable;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/22/12
 * Time: 11:21 AM
 */
public class Cas implements Serializable {
    public boolean isLogin = false;

    public String loginName;
    
    public Resaler resaler;
}
