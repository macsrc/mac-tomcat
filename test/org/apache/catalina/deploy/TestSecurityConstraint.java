package org.apache.catalina.deploy;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.HttpConstraintElement;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;

import junit.framework.TestCase;

public class TestSecurityConstraint extends TestCase {

    private static final String URL_PATTERN = "/test";
    private static final String ROLE1 = "R1";

    /**
     * Uses the examples in SRV.13.4 as the basis for these tests
     */
    public void testCreateConstraints() {
        
        ServletSecurityElement element;
        SecurityConstraint[] result;
        Set<HttpMethodConstraintElement> hmces =
            new HashSet<HttpMethodConstraintElement>();
        
        // Example 13-1
        // @ServletSecurity 
        element = new ServletSecurityElement();
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);

        assertEquals(0, result.length);

        // Example 13-2
        // @ServletSecurity(
        //     @HttpConstraint(
        //         transportGuarantee = TransportGuarantee.CONFIDENTIAL))
        element = new ServletSecurityElement(
                new HttpConstraintElement(
                        ServletSecurity.TransportGuarantee.CONFIDENTIAL));
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);

        assertEquals(1, result.length);
        assertEquals(false, result[0].getAuthConstraint());
        assertEquals(true,
                result[0].findCollections()[0].findPattern(URL_PATTERN));
        assertEquals(0, result[0].findCollections()[0].findMethods().length);
        assertEquals(ServletSecurity.TransportGuarantee.CONFIDENTIAL.name(),
                result[0].getUserConstraint());
        
        // Example 13-3
        // @ServletSecurity(@HttpConstraint(EmptyRoleSemantic.DENY)) 
        element = new ServletSecurityElement(
                new HttpConstraintElement(EmptyRoleSemantic.DENY));
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);

        assertEquals(1, result.length);
        assertEquals(true, result[0].getAuthConstraint());
        assertEquals(true,
                result[0].findCollections()[0].findPattern(URL_PATTERN));
        assertEquals(0, result[0].findCollections()[0].findMethods().length);
        assertEquals(ServletSecurity.TransportGuarantee.NONE.name(),
                result[0].getUserConstraint());
        
        // Example 13-4
        // @ServletSecurity(@HttpConstraint(rolesAllowed = "R1")) 
        element = new ServletSecurityElement(new HttpConstraintElement(
                ServletSecurity.TransportGuarantee.NONE, ROLE1));
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);

        assertEquals(1, result.length);
        assertEquals(true, result[0].getAuthConstraint());
        assertEquals(1, result[0].findAuthRoles().length);
        assertEquals(true, result[0].findAuthRole(ROLE1));
        assertEquals(true,
                result[0].findCollections()[0].findPattern(URL_PATTERN));
        assertEquals(0, result[0].findCollections()[0].findMethods().length);
        assertEquals(ServletSecurity.TransportGuarantee.NONE.name(),
                result[0].getUserConstraint());

        // Example 13-5
        // @ServletSecurity((httpMethodConstraints = { 
        //     @HttpMethodConstraint(value = "GET", rolesAllowed = "R1"), 
        //     @HttpMethodConstraint(value = "POST", rolesAllowed = "R1", 
        //     transportGuarantee = TransportGuarantee.CONFIDENTIAL) 
        // })
        hmces.clear();
        hmces.add(new HttpMethodConstraintElement("GET",
                new HttpConstraintElement(
                        ServletSecurity.TransportGuarantee.NONE, ROLE1)));
        hmces.add(new HttpMethodConstraintElement("POST",
                new HttpConstraintElement(
                        ServletSecurity.TransportGuarantee.CONFIDENTIAL,
                        ROLE1)));
        element = new ServletSecurityElement(hmces);
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);
        
        assertEquals(2, result.length);
        for (int i = 0; i < 2; i++) {
            assertEquals(true, result[i].getAuthConstraint());
            assertEquals(1, result[i].findAuthRoles().length);
            assertEquals(true, result[i].findAuthRole(ROLE1));
            assertEquals(true,
                    result[i].findCollections()[0].findPattern(URL_PATTERN));
            assertEquals(1, result[i].findCollections()[0].findMethods().length);
            String method = result[i].findCollections()[0].findMethods()[0];
            if ("GET".equals(method)) {
                assertEquals(ServletSecurity.TransportGuarantee.NONE.name(),
                        result[i].getUserConstraint());
            } else if ("POST".equals(method)) {
                assertEquals(ServletSecurity.TransportGuarantee.CONFIDENTIAL.name(),
                        result[i].getUserConstraint());
            } else {
                fail("Unexpected method :[" + method + "]");
            }
        }
        
        // Example 13-6
        // @ServletSecurity(value = @HttpConstraint(rolesAllowed = "R1"), 
        //     httpMethodConstraints = @HttpMethodConstraint("GET"))
        hmces.clear();
        hmces.add(new HttpMethodConstraintElement("GET"));
        element = new ServletSecurityElement(
                new HttpConstraintElement(
                        ServletSecurity.TransportGuarantee.NONE,
                        ROLE1),
                hmces);
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);
        
        assertEquals(2, result.length);
        for (int i = 0; i < 2; i++) {
            assertEquals(true,
                    result[i].findCollections()[0].findPattern(URL_PATTERN));
            if (result[i].findCollections()[0].findMethods().length == 1) {
                assertEquals("GET",
                        result[i].findCollections()[0].findMethods()[0]);
                assertEquals(false, result[i].getAuthConstraint());
            } else if (result[i].findCollections()[0].findOmittedMethods().length == 1) {
                assertEquals("GET",
                        result[i].findCollections()[0].findOmittedMethods()[0]);
                assertEquals(true, result[i].getAuthConstraint());
                assertEquals(1, result[i].findAuthRoles().length);
                assertEquals(ROLE1, result[i].findAuthRoles()[0]);
            } else {
                fail("Unexpected number of methods defined");
            }
            assertEquals(ServletSecurity.TransportGuarantee.NONE.name(),
                    result[i].getUserConstraint());
        }
        
        // Example 13-7
        // @ServletSecurity(value = @HttpConstraint(rolesAllowed = "R1"), 
        //     httpMethodConstraints = @HttpMethodConstraint(value="TRACE", 
        //         emptyRoleSemantic = EmptyRoleSemantic.DENY))
        hmces.clear();
        hmces.add(new HttpMethodConstraintElement("TRACE",
                new HttpConstraintElement(EmptyRoleSemantic.DENY)));
        element = new ServletSecurityElement(
                new HttpConstraintElement(
                        ServletSecurity.TransportGuarantee.NONE,
                        ROLE1),
                hmces);
        result = SecurityConstraint.createConstraints(element, URL_PATTERN);
        
        assertEquals(2, result.length);
        for (int i = 0; i < 2; i++) {
            assertEquals(true,
                    result[i].findCollections()[0].findPattern(URL_PATTERN));
            if (result[i].findCollections()[0].findMethods().length == 1) {
                assertEquals("TRACE",
                        result[i].findCollections()[0].findMethods()[0]);
                assertEquals(true, result[i].getAuthConstraint());
                assertEquals(0, result[i].findAuthRoles().length);
            } else if (result[i].findCollections()[0].findOmittedMethods().length == 1) {
                assertEquals("TRACE",
                        result[i].findCollections()[0].findOmittedMethods()[0]);
                assertEquals(true, result[i].getAuthConstraint());
                assertEquals(1, result[i].findAuthRoles().length);
                assertEquals(ROLE1, result[i].findAuthRoles()[0]);
            } else {
                fail("Unexpected number of methods defined");
            }
            assertEquals(ServletSecurity.TransportGuarantee.NONE.name(),
                    result[i].getUserConstraint());
        }
        
        // Example 13-8 is the same as 13-4
        // Example 13-9 is the same as 13-7
    }

}
