package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    private static final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lockedUntil = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME = 5L * 60 * 1000;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String errorMessage = "";
        boolean isSuccess = false;
        Staff staff = null;

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {

            errorMessage = "Invalid email or password.";

        } else {

            email = email.trim();
            password = password.trim();

            if (isAccountLocked(email)) {

                long remain = (lockedUntil.get(email) - System.currentTimeMillis()) / 1000;

                errorMessage = "Your account is locked. Please try again after "
                        + remain + " seconds.";

            } else {

                staff = authenticateUser(email, password);

                if (staff != null) {

                    isSuccess = true;
                    resetLoginAttempt(email);

                } else {

                    errorMessage = handleFailedLogin(email);

                }
            }
        }

        if (isSuccess) {

            staff.setPassword(null);
            HttpSession session = request.getSession();
            session.setAttribute("user", staff);
            response.sendRedirect("staff-list");

        } else {

            request.setAttribute("error", errorMessage);
            request.getRequestDispatcher("login.jsp").forward(request, response);

        }
    }

    private boolean isAccountLocked(String email) {

        Long lockTime = lockedUntil.get(email);

        if (lockTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= lockTime) {

            resetLoginAttempt(email);
            return false;
        }

        return true;
    }

    private void increaseFailedAttempt(String email) {

        int count = failedAttempts.getOrDefault(email, 0) + 1;

        if (count >= MAX_ATTEMPTS) {

            lockedUntil.put(email,
                    System.currentTimeMillis() + LOCK_TIME);

            failedAttempts.remove(email);

        } else {

            failedAttempts.put(email, count);

        }
    }

    private void resetLoginAttempt(String email) {

        failedAttempts.remove(email);
        lockedUntil.remove(email);

    }
    
    private Staff authenticateUser(String email, String password) {
        StaffDAO staffDAO = new StaffDAO();
        return staffDAO.checkLogin(email, password);
    }
    
    private String handleFailedLogin(String email) {

        increaseFailedAttempt(email);

        if (isAccountLocked(email)) {
            return "Your account has been locked for 5 minutes.";
        }

        int count = failedAttempts.getOrDefault(email, 0);

        return "Invalid email or password. Attempt "
                + count + "/" + MAX_ATTEMPTS;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
    
}
