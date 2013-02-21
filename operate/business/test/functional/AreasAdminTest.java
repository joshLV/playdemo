package functional;


import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.operator.OperateUser;
import models.sales.Area;
import models.sales.Category;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-9
 * Time: 下午1:53
 */
public class AreasAdminTest extends FunctionalTest {
    Area area;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        area = FactoryBoy.create(Area.class);


    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/areas?parentId=" + area.parent.id);
        assertStatus(200, response);
        List<Area> areaList = (List<Area>) renderArgs("areaList");
        assertEquals(1, areaList.size());
        assertEquals(area.name, areaList.get(0).name);
    }

    @Test
    public void testIndexAreaNoParent() {
        Http.Response response = GET("/areas");
        assertStatus(200, response);
        List<Area> areaList = (List<Area>) renderArgs("areaList");
        assertEquals(1, areaList.size());
        assertEquals(null, renderArgs("parentId"));
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/areas/" + area.id + "/edit?parentId=" + area.parent.id);
        assertStatus(200, response);
        assertEquals(area.name, renderArgs("area.name"));
    }


    @Test
    public void testUpdate() {
        String name = "新区域";
        String params = "area.name=" + name;
        Http.Response response = PUT("/areas/" + area.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        area.refresh();
        assertEquals(name, area.name);
    }

    @Test
    public void testAddAreaNoParent() {
        Http.Response response = GET("/areas/new");
        assertStatus(200, response);
        assertEquals(null, renderArgs("parentId"));
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/areas/new?parentId=" + area.parent.id);
        assertStatus(200, response);
        assertEquals(area.parent.id.toString(), (String) renderArgs("parentId"));
    }

    @Test
    public void testCreate() {
        assertEquals(3, Area.count());
        Map<String, String> itemParams = new HashMap<>();
        String name = "新区域1";
        itemParams.put("area.id", "02189");
        itemParams.put("area.name", name);
        itemParams.put("area.displayOrder", String.valueOf(area.displayOrder));
        itemParams.put("area.areaType", area.areaType.toString());
        itemParams.put("area.parent", area.parent.toString());
        Http.Response response = POST("/areas", itemParams);
        assertStatus(302, response);
        assertEquals(4, Area.count());
    }



    @Test
    public void testDelete() {
        List<Area> areaList = Area.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        assertEquals(3, areaList.size());
        Http.Response response = DELETE("/areas/" + area.id);
        assertStatus(302, response);
        areaList = Category.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        assertEquals(0, areaList.size());
    }


}
