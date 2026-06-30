package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the thin HTTP wiring of {@link LoginServlet} after the auth/lockout
 * logic was extracted to {@code AuthService}. The servlet is created INSIDE the
 * {@code mockConstruction} block so the {@code StaffDAO} built by
 * {@code new AuthService()} is the mocked one. Auth/lockout rules themselves are
 * covered by {@code AuthServiceTest}.
 */
class LoginServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    void testDoPost_Success_storesUserAndRedirects() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("123456");

        Staff mockStaff = new Staff();
        mockStaff.setEmail("admin@gmail.com");

        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (m, ctx) -> when(m.checkLogin("admin@gmail.com", "123456")).thenReturn(mockStaff))) {

            LoginServlet servlet = new LoginServlet();
            servlet.doPost(request, response);

            verify(session).setAttribute("user", mockStaff);
            verify(response).sendRedirect("staff-list");
        }
    }

    @Test
    void testDoPost_EmptyEmailOrPassword_forwardsWithError() throws Exception {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        // No DAO call on this path, so a real StaffDAO is harmless.
        new LoginServlet().doPost(request, response);

        verify(request).setAttribute("error", "Invalid email or password.");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void testDoPost_FailedLogin_showsAttemptCount() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("wrongpass");

        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (m, ctx) -> when(m.checkLogin(anyString(), anyString())).thenReturn(null))) {

            LoginServlet servlet = new LoginServlet();
            servlet.doPost(request, response);

            ArgumentCaptor<String> error = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("error"), error.capture());
            assertTrue(error.getValue().contains("Attempt 1/5"));
            verify(requestDispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_AccountLockedAfterMaxAttempts() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("wrongpass");

        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (m, ctx) -> when(m.checkLogin(anyString(), anyString())).thenReturn(null))) {

            LoginServlet servlet = new LoginServlet();
            for (int i = 1; i <= 6; i++) {
                servlet.doPost(request, response);
            }

            ArgumentCaptor<String> error = ArgumentCaptor.forClass(String.class);
            verify(request, org.mockito.Mockito.atLeast(1)).setAttribute(eq("error"), error.capture());
            String last = error.getAllValues().get(error.getAllValues().size() - 1);
            assertTrue(last.contains("locked") || last.contains("try again after"),
                    "Expected lock message, got: " + last);
        }
    }
}
