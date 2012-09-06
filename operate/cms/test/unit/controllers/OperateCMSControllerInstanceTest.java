package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;
/**
 * @author likang
 */
public class OperateCMSControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new WEBApplication() instanceof Controller);
        assertTrue(new CmsBlocks() instanceof Controller);
        assertTrue(new OperateTopics() instanceof Controller);
    }
}
