package com.uhuila.utils;

import java.io.File;
import java.util.Properties;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;

public class SmartPropertyPlaceholderConfigurer extends
        PropertyPlaceholderConfigurer 
        implements BeanNameAware, BeanFactoryAware {

    private Properties _properties;
    
    /**
     * 传入一个文件名，然后检查是否在指定的reconfig.dir中有这个文件，如果有就使用之。
     * 如果没有，检查classpath中有没有，有则使用之。
     * @param fileName
     */
    public void setSmartLocationFileName(String fileName) {
        String strReconfigDir = System.getProperty("reconfig.dir",  "/etc/reeb/cas"); 
        
        File reconfigDir = new File(strReconfigDir);
//        System.out.println("strConfigDir=" + strReconfigDir);
        if (reconfigDir.exists() && reconfigDir.isDirectory()) {
            File reconfigFile = new File(reconfigDir, fileName);
            if (reconfigFile.exists()) {
                setLocation(new FileSystemResource(reconfigFile));
                return;
            }
        } else {
            DefaultResourceLoader loader = new DefaultResourceLoader();
            setLocation(loader.getResource("classpath:" + fileName));
        }
        
    }
    
    public Properties getMergedProperties() throws Exception {
        if (_properties == null) {
            _properties = this.mergeProperties();
        }
        return _properties;
    }
    

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String result;
        try {
            result = getMergedProperties().getProperty(placeholder);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        if (result == null) {
            return super.resolvePlaceholder(placeholder, props);
        } else {
            return result;
        }
    }
    
    
}
