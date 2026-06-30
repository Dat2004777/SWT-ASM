package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.PageResult;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/staff-list")
public class StaffListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer staffId = parseNullable(request.getParameter("staffId"));
        int page = parsePage(request.getParameter("page"));
        PageResult<Staff> result = new StaffDAO().search(
                request.getParameter("searchName"), staffId,
                request.getParameter("searchStatus"),
                request.getParameter("searchDepartment"), page, 10);
        request.setAttribute("staffList", result.getItems());
        request.setAttribute("pageResult", result);
        request.getRequestDispatcher("staff-list.jsp").forward(request, response);
    }

    private Integer parseNullable(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (Exception e) {
            return 1;
        }
    }
}
