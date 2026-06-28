package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Staff;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String urlPath = requestURI.substring(contextPath.length());

        if (urlPath.startsWith("/assets/") || urlPath.startsWith("/css/") || urlPath.startsWith("/js/")
                || urlPath.equals("/login") || urlPath.equals("/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        Staff loggedInUser = (session != null) ? (Staff) session.getAttribute("user") : null;

        if (loggedInUser == null) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        if (urlPath.equals("/staff-crud")) {
            if (loggedInUser.getRole() == null || loggedInUser.getRole().getRoleID() != 1) {
                
                httpRequest.setAttribute("error", "Tài khoản của bạn không có quyền truy cập khu vực CRUD này!");
                httpRequest.getRequestDispatcher("/staff-list").forward(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}