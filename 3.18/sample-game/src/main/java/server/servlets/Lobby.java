package server.servlets;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

// See servlet mapping for URLs - web.xml
public class Lobby extends HttpServlet {

    // Handle GET requests
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get session for reuse
        HttpSession session = request.getSession();

        // Check if authorized (session attribute is set) - redirect if NOT
        if (session.getAttribute("sessionUser") == null) {
            response.sendRedirect("/signin");           // redirect to sign in
            return;
        }

        // If 'sessionUser' is NOT null (already signed in) - set 'lobby.jsp' as URL and forward
        RequestDispatcher rd = request.getRequestDispatcher("lobby.jsp");
        rd.forward(request, response);
    }

    // Handle POST requests
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Redirect POST as GET (loopback to the same servlet)
        response.sendRedirect("/lobby");
    }
}
