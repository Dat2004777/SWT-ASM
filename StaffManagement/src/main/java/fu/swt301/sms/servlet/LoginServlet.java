package fu.swt301.sms.servlet;

import fu.swt301.sms.service.AuthService;
import fu.swt301.sms.service.LoginResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Single instance per servlet keeps the lockout state shared across requests.
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        LoginResult result = authService.login(email, password);

        if (result.isSuccess()) {
            HttpSession session = request.getSession();
            session.setAttribute("user", result.getStaff());
            response.sendRedirect("staff-list");
        } else {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}
