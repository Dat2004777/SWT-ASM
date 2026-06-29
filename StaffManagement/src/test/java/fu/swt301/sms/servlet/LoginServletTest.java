package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LoginServletTest {

    private LoginServlet loginServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() throws Exception {
        loginServlet = new LoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        resetStaticState();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetStaticState();
    }

    private void resetStaticState() throws Exception {
        Field failedAttemptsField = LoginServlet.class.getDeclaredField("failedAttempts");
        failedAttemptsField.setAccessible(true);
        Map<?, ?> failedAttempts = (Map<?, ?>) failedAttemptsField.get(null);
        failedAttempts.clear();

        Field lockedUntilField = LoginServlet.class.getDeclaredField("lockedUntil");
        lockedUntilField.setAccessible(true);
        Map<?, ?> lockedUntil = (Map<?, ?>) lockedUntilField.get(null);
        lockedUntil.clear();
    }

    @Test
    void testDoPost_Success() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("123456");

        Staff mockStaff = new Staff();
        mockStaff.setEmail("admin@gmail.com");
        
        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (mock, context) -> {
                    when(mock.checkLogin("admin@gmail.com", "123456")).thenReturn(mockStaff);
                })) {

            loginServlet.doPost(request, response);

            verify(session).setAttribute("user", mockStaff);
            verify(response).sendRedirect("staff-list");
        }
    }

    @Test
    void testDoPost_EmptyEmailOrPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        loginServlet.doPost(request, response);

        verify(request).setAttribute("error", "Invalid email or password.");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void testDoPost_FailedLogin_IncreasesAttemptCount() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("wrongpass");

        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (mock, context) -> {
                    when(mock.checkLogin(anyString(), anyString())).thenReturn(null);
                })) {

            loginServlet.doPost(request, response);

            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request).setAttribute(eq("error"), errorCaptor.capture());
            assertTrue(errorCaptor.getValue().contains("Attempt 1/5"));
            
            verify(requestDispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_AccountLockedAfterMaxAttempts() throws Exception {
        when(request.getParameter("email")).thenReturn("admin@gmail.com");
        when(request.getParameter("password")).thenReturn("wrongpass");

        try (MockedConstruction<StaffDAO> mocked = mockConstruction(StaffDAO.class,
                (mock, context) -> {
                    when(mock.checkLogin(anyString(), anyString())).thenReturn(null);
                })) {

            for (int i = 1; i <= 5; i++) {
                loginServlet.doPost(request, response);
            }

            loginServlet.doPost(request, response);

            ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
            verify(request, atLeast(1)).setAttribute(eq("error"), errorCaptor.capture());
            
            String lastError = errorCaptor.getAllValues().get(errorCaptor.getAllValues().size() - 1);
            assertTrue(lastError.contains("locked") || lastError.contains("try again after"), 
                "Expected lock message, got: " + lastError);
        }
    }
}
