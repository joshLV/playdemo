package com.uhuila.operate.cas;

public class DomainNameUtil {

    public static String[] getDomainUser(String fullUserName) {
        if (fullUserName.indexOf("@") < 0) {
            return null;
        }
        return fullUserName.split("@", 2);
    }

    public static String getSubdomain(String serverName) {
        if (serverName.indexOf('.') < 0) {
            return serverName;
        }
        
        return serverName.substring(0, serverName.indexOf('.'));
    }
}
