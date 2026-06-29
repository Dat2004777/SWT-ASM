package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LogoutServletTest {

    private LogoutServlet logoutServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        logoutServlet = new LogoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testDoGet_WithLoggedInUser() throws Exception {
        Staff mockStaff = new Staff();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(mockStaff);
        when(request.getContextPath()).thenReturn("/sms");

        logoutServlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/sms/login");
    }

    @Test
    void testDoGet_WithoutLoggedInUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/sms");

        logoutServlet.doGet(request, response);

        verify(session, never()).invalidate();
        verify(response).sendRedirect("/sms/login");
    }
}
