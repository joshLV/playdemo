package com.uhuila.common.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * 文件上传工具类.
 * <p/>
 * User: sujie
 * Date: 4/1/12
 * Time: 2:49 PM
 */
public class FileUploadUtilTest extends TestCase {
    @Test
    public void testGetPathById() throws Exception {
        URL testUrl = FileUploadUtilTest.class.getClassLoader().getResource("test.jpg");
        File image = new File(testUrl.getFile());
        String targetPath = FileUploadUtil.storeImage(image, 12L, "/tmp");
        assertEquals("/tmp/0/0/12/12.jpg", targetPath);
        targetPath = FileUploadUtil.storeImage(image, 24L, false, "/tmp");
        assertEquals("/tmp/0/0/24/test.jpg", targetPath);
        targetPath = FileUploadUtil.storeImage(image, "/tmp");
        assertEquals("/tmp/", targetPath.substring(0, 5));
        assertEquals(".jpg", targetPath.substring(targetPath.length() - 4, targetPath.length()));
    }
}
