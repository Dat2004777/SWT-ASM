package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.AuthService;
import fu.swt301.sms.service.LoginResult;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the HTTP wiring of {@link LoginServlet}.
 * The servlet delegates authentication and lockout logic to {@code AuthService}.
 * We mock {@code AuthService} to isolate the servlet's request/response handling.
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
        LoginResult successResult = LoginResult.success(mockStaff);

        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (m, ctx) -> when(m.login("admin@gmail.com", "123456")).thenReturn(successResult))) {

            LoginServlet servlet = new LoginServlet();
            servlet.doPost(request, response);

            verify(session).setAttribute("user", mockStaff);
            verify(response).sendRedirect("staff-list");
        }
    }

    @Test
    void testDoPost_Failure_setsErrorAndForwards() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("wrongpass");

        LoginResult failureResult = LoginResult.failure("Invalid email or password.");

        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (m, ctx) -> when(m.login("admin@gmail.com", "wrongpass")).thenReturn(failureResult))) {

            LoginServlet servlet = new LoginServlet();
            servlet.doPost(request, response);

            verify(request).setAttribute("error", "Invalid email or password.");
            verify(requestDispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_forwardsToLoginJsp() throws Exception {
        // GET request shouldn't trigger any login logic, so AuthService mock is just to prevent real initialization side-effects
        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class)) {
            LoginServlet servlet = new LoginServlet();
            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("login.jsp");
            verify(requestDispatcher).forward(request, response);
        }
    }
}
