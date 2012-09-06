package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class OperateAdminControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new WEBApplication() instanceof Controller);
        assertTrue(new OperateUsers() instanceof Controller);
        assertTrue(new OperateUsersPassword() instanceof Controller);
        assertTrue(new OperateUsersProfiles() instanceof Controller);
    }
}
