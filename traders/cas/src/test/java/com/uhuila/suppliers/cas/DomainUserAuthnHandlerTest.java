package com.uhuila.suppliers.cas;

import static org.junit.Assert.*;

import org.junit.Test;

public class DomainUserAuthnHandlerTest {
    
    private DomainUserAuthnHandler handler = new DomainUserAuthnHandler();

    @Test
    public void testSpliteUserName() {
        String[] domainUser = handler.getDomainUser("test1@lyf");
        assertEquals("test1", domainUser[0]);
        assertEquals("lyf", domainUser[1]);
        
        assertNull(handler.getDomainUser("invalidUser"));
    }

}
