package unit.controllers;

import controllers.SmsApplication;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class MQControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new SmsApplication() instanceof Controller);
    }
}
