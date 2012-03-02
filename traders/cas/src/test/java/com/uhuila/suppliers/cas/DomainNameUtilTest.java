package com.uhuila.suppliers.cas;

import static org.junit.Assert.*;

import org.junit.Test;

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
        assertEquals("lyf", DomainNameUtil.getSubdomain("lyf.supplierdev.com"));
        assertEquals("test1", DomainNameUtil.getSubdomain("test1.supplierdev.com"));
        
        assertEquals("localhost", DomainNameUtil.getSubdomain("localhost"));
    }
    
}
