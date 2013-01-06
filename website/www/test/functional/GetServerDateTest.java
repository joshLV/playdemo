package functional;

import controllers.GetServerDate;
import org.junit.Test;
import play.mvc.Controller;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用于秒杀，得到当前服务器时间。
 * User: tanglq
 * Date: 13-1-5
 * Time: 上午9:36
 */
public class GetServerDateTest extends FunctionalTest {

    @Test
    public void testGetTime() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("MMM d,yyyy HH:mm:ss", Locale.ENGLISH);

        Http.Response response = GET("/server_time?t=test");
        assertIsOk(response);

        Date dateTime = format.parse(getContent(response));

        // 得到的时间与当前时间差要小于2秒. 服务器可能太慢
        assertTrue(((new Date()).getTime() - dateTime.getTime()) < 2000);
    }

    @Test
    public void testInstance() throws Exception {
        assertTrue((new GetServerDate) instanceof Controller);
    }
}
