/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 *
 * @author dat20
 */
public class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    public void setUp() {
        filter = new AuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/StaffManagement");
    }

    @Test
    public void testDoFilter_ShouldBypass_WhenAccessingLogin() throws Exception {
        when(request.getRequestURI()).thenReturn("/StaffManagement/login");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilter_ShouldRedirectToLogin_WhenUserNotLoggedIn() throws Exception {
        when(request.getRequestURI()).thenReturn("/StaffManagement/staff-list");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response, times(1)).sendRedirect("/StaffManagement/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void testDoFilter_ShouldAllowAdmin_ToAccessCrud() throws Exception {
        when(request.getRequestURI()).thenReturn("/StaffManagement/staff-crud");
        when(request.getSession(false)).thenReturn(session);

        Staff admin = new Staff();
        Role role = new Role();
        role.setRoleID(1);
        admin.setRole(role);
        when(session.getAttribute("user")).thenReturn(admin);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilter_ShouldBlockStaff_FromAccessingCrud() throws Exception {
        when(request.getRequestURI()).thenReturn("/StaffManagement/staff-crud");
        when(request.getContextPath()).thenReturn("/StaffManagement");
        when(request.getSession(false)).thenReturn(session);
        
        Staff staff = new Staff();
        Role role = new Role();
        role.setRoleID(2); 
        staff.setRole(role);
        when(session.getAttribute("user")).thenReturn(staff);

        filter.doFilter(request, response, chain);

        verify(response, times(1)).sendRedirect("/StaffManagement/staff-list");
        verify(chain, never()).doFilter(request, response);
    }
}
