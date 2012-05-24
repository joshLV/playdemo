package com.uhuila.utils;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.Properties;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class SmartPropertyPlaceholderConfigurerTest {

    @Test
    public void testSetSmartLocation() throws Exception {
        SmartPropertyPlaceholderConfigurer config = new SmartPropertyPlaceholderConfigurer();
        config.setSmartLocationFileName("test.properties");
        Properties prop = config.getMergedProperties();
        assertEquals("Hello", prop.getProperty("test.msg"));
    }


    @Test
    public void testSetSmartLocationFromFile() throws Exception {
        SmartPropertyPlaceholderConfigurer config = new SmartPropertyPlaceholderConfigurer();
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource r = loader.getResource("classpath:test.properties");
        File testFile = r.getFile();
        String path = testFile.getParentFile().getAbsolutePath();
        System.setProperty("reconfig.dir", path + "/reconfig");
        config.setSmartLocationFileName("file.properties");
        Properties prop = config.getMergedProperties();
        
        assertEquals("World", prop.getProperty("test.msg"));
    }
    
}
