/**
 *  This file is part of LogiSima-play-cas.
 *
 *  LogiSima-play-cas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  LogiSima-play-cas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with LogiSima-play-cas.  If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.supplier.cas;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import play.Logger;
import play.Play;
import play.supplier.cas.models.CASUser;
import play.mvc.Scope;
import play.mvc.results.Forbidden;
import play.utils.Java;

/**
 * The Security class interface. This is the entry point where you can plug your own security manager, like how to check
 * rights, how to define your own user object and put in cache (not in session !!).
 * 
 * @author bsimard
 * 
 */
public class Security {

    private static String _loginNameForTest = null;
    
    /**
     * 设置当前线程为已经登录。只在DEV模式下有效。
     * 注意: 只应用于FunctionTest!
     * @param logined
     */
    public static void setLoginUserForTest(String login) {
        if (Play.mode == Play.Mode.DEV) {
            _loginNameForTest = login;
        }
    }
    
    /**
     * 在@After方法中要调用一下这个，以避免影响selenium.
     */
    public static void cleanLoginUserForTest() {
        _loginNameForTest = null;
    }
      
    public static String getLoginUserForTest() {
        if (Play.mode == Play.Mode.DEV) {
            return _loginNameForTest;
        }
        return null;
    }
    
    public static boolean isTestLogined() {
        if (Play.mode != Play.Mode.DEV) {
            return false;
        }
        return _loginNameForTest != null;
    }
    
    /**
     * Method to check user's profile.
     * 
     * @param profile
     * @return
     */
    public static boolean check(String profile) {
        return true;
    }

    /**
     * Method that return the user object. By default, it's only check session and return the username.
     * 
     * @return
     */
    public static Object connected() {
        return Scope.Session.current().get("username");
    }

    /**
     * Method that check if the user if connected.
     * 
     * @return
     */
    public static boolean isConnected() {
        return Scope.Session.current().contains("username");
    }

    /**
     * This method is called just after the user authentification (when ST is validated).
     */
    static void onAuthenticated(CASUser user) {
        Logger.debug("[SecureCAS]: onAutenticated method");
    }

    /**
     * This method is called just before the logout route.
     */
    static void onDisconnected() {
    }

    /**
     * This method is called when the user have not the good profile.
     * 
     * @param profile
     */
    static void onCheckFailed(String profile) {
        Logger.debug("[SecureCAS]: profile " + profile + " check failed");
        throw new Forbidden("Access denied");
    }

    /**
     * Method to find the good Security class. If there is class that extends this one, we take it !
     * 
     * @param m
     * @param args
     * @return
     * @throws Throwable
     */
    public static Object invoke(String m, Object... args) throws Throwable {
        Logger.info(m);
        Class security = null;
        List<Class> classes = Play.classloader.getAssignableClasses(Security.class);
        if (classes.size() == 0) {
            security = Security.class;
        }
        else {
            security = classes.get(0);
        }
        try {
            return Java.invokeStaticOrParent(security, m, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
