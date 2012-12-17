package unit.controllers;

import controllers.*;
import controllers.UploadFiles;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class SupplierSalesControllersInstanceTest extends UnitTest {
    @Test
    public void controllerInstancesTest(){
        assertTrue(new Categories() instanceof Controller);
        assertTrue(new Shops() instanceof Controller);
        assertTrue(new SupplierGoods() instanceof Controller);
        assertTrue(new UploadFiles() instanceof Controller);
    }

}
