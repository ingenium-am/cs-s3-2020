package server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// See servlet mapping for URLs - web.xml
public class LogOut extends HttpServlet {

    // Handle GET requests
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Remove user data from session
        request.getSession().removeAttribute("sessionUserId");
        request.getSession().removeAttribute("sessionUser");

        response.sendRedirect("/");
    }
}
