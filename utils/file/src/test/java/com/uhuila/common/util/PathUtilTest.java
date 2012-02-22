package com.uhuila.common.util;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * 文件路径生成器的测试类.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 1:31 PM
 */
public class PathUtilTest extends TestCase {
    @Test
    public void testGetPathById() throws Exception {
        assertEquals("/0/0/0/", PathUtil.getPathById(0));
        assertEquals("/0/0/1/", PathUtil.getPathById(1));
        assertEquals("/0/1/0/", PathUtil.getPathById(1024));
        assertEquals("/0/10/0/", PathUtil.getPathById(10240));
        assertEquals("/1/0/0/", PathUtil.getPathById(1024 * 1024));
        assertEquals("/1/0/123/", PathUtil.getPathById(1024 * 1024 + 123));
    }
}
