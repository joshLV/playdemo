import org.apache.commons.codec.digest.DigestUtils;
import org.junit.*;

import common.CharacterUtil;

import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
        String randnum= CharacterUtil.getRandomString(6);
        System.out.println(randnum);
        System.out.println(DigestUtils.md5Hex("123456"+randnum));
    }

}
