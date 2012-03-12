package unit;

import java.util.List;

import models.admin.SupplierNavigation;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class SupplierNavigationTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        Fixtures.delete(SupplierNavigation.class);
        Fixtures.loadModels("fixture/navigation_unit.yml");
    }

    @Test
    public void testGetTopNavigations() {
        List<SupplierNavigation> topMenus = SupplierNavigation.getTopNavigations();
        assertEquals(2, topMenus.size());
        // display_order test
        assertEquals("main", topMenus.get(0).name);
        assertEquals("order", topMenus.get(1).name);
    }
    
    @Test
    public void testGetNavigationParentStack() {
        List<SupplierNavigation> navigationStack = SupplierNavigation.getNavigationParentStack("user_add");
        
        assertEquals(3, navigationStack.size());
        // navigation stack order test
        assertEquals("main", navigationStack.get(0).name);
        assertEquals("user", navigationStack.get(1).name);
        assertEquals("user_add", navigationStack.get(2).name);
    }
    
    @Test
    public void testGetSecendLevelNavigations() {
        List<SupplierNavigation> secendLevelNavigations = SupplierNavigation.getSecendLevelNavigations("user_edit");
        assertEquals(2, secendLevelNavigations.size());
        // secend level menu order test
        assertEquals("user", secendLevelNavigations.get(0).name);
        assertEquals("role", secendLevelNavigations.get(1).name);
    }
}
