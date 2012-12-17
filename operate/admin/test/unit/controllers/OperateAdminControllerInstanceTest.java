package unit.controllers;

import controllers.OperateAdminApplication;
import controllers.OperateUsers;
import controllers.OperateUsersPassword;
import controllers.OperateUsersProfiles;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class OperateAdminControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new OperateAdminApplication() instanceof Controller);
        assertTrue(new OperateUsers() instanceof Controller);
        assertTrue(new OperateUsersPassword() instanceof Controller);
        assertTrue(new OperateUsersProfiles() instanceof Controller);
    }
}
