package models.resaletrace;

import java.io.Serializable;

import models.resale.Resaler;

/**
 * <p/>
 * User: yjy
 * Date: 3/22/12
 * Time: 11:21 AM
 */
public class Cas implements Serializable {
    public boolean isLogin = false;

    public String username;
    
    public Resaler resaler;
}
