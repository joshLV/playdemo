package com.uhuila.common.util;
/**
 * .
 * User: sujie
 * Date: 2/7/12
 * Time: 5:31 PM
 */

import com.uhuila.common.util.util.PathUtil;
import org.junit.Test;
import play.test.UnitTest;

public class PathUtilTest extends UnitTest {
    @Test
    public void testGetPathById() throws Exception {
        assertEquals("0/0/0/", PathUtil.getPathById(0));
        assertEquals("0/0/1/", PathUtil.getPathById(1));
        assertEquals("0/1/0/", PathUtil.getPathById(1024));
        assertEquals("0/10/0/", PathUtil.getPathById(10240));
        assertEquals("1/0/0/", PathUtil.getPathById(1024*1024));
        assertEquals("1/0/123/", PathUtil.getPathById(1024 * 1024 + 123));
    }
}