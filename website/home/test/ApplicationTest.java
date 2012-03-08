
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import models.*;

import org.junit.*;
import org.junit.Before;

import play.mvc.*;
import play.mvc.Http.*;
import play.test.*;

public class ApplicationTest extends FunctionalTest {

    @Before
    public void loadData() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels("fixture/user.yml");
    }


    @Ignore
    @Test
    public void testThatIndexPageWorks() {
        System.out.println("hello");
        //clearCookies();

        loginForTest("selenium@uhuila.com");
        Response response1 = GET("/orders");
        assertStatus(302, response1);
    }

    public void loginForTest(String user) {
        clearCookies();
        Response response0 = GET("/login");

        Request request = newRequest();
        makeRequest(request, response0);

        System.out.println("user2=" + user);
        Map<String, String> params = new HashMap<String, String>();
        params.put("login", user);
        params.put("password", user);

        Response response = POST(request, "/@cas/authenticate", params, new HashMap<String, File>());
        for(String name : response.cookies.keySet()) {
            System.out.println("name=" + name + ", value=" + response.cookies.get(name).value);
        }
        //assertIsOk(response);

        Request request0 = newRequest();
        makeRequest(request0, response0);
    }

}