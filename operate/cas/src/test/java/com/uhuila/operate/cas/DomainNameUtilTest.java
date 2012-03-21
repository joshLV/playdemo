package com.uhuila.operate.cas;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DomainNameUtilTest {
    
    @Test
    public void testSpliteUserName() {
        String[] domainUser = DomainNameUtil.getDomainUser("test1@lyf");
        assertEquals("test1", domainUser[0]);
        assertEquals("lyf", domainUser[1]);
        
        assertNull(DomainNameUtil.getDomainUser("invalidUser"));
    }
	
    @Test
    public void testGetSubdomain() {
        assertEquals("lyf", DomainNameUtil.getSubdomain("lyf.uhuila.net"));
        assertEquals("test1", DomainNameUtil.getSubdomain("test1.uhuila.net"));
        
        assertEquals("localhost", DomainNameUtil.getSubdomain("localhost"));
    }
    
}
