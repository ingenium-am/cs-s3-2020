package server.servlets;

import dbservices.dao.UserDAO;
import app.data.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// See servlet mapping for URLs - web.xml
public class SignIn extends HttpServlet {

    // Handle GET requests
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // Set 'signin.html' as URL and forward
        RequestDispatcher rd = request.getRequestDispatcher("signin.html");
        rd.forward(request, response);
    }

    // Handle POST requests
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Get request parameters (see signin.html)
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        // If no login or password submitted - response 400
        if (login == null || password == null) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Get User by submitted login
        User user = UserDAO.getSingleton().getUserByLogin(login);

        // If User not found - respond by alert text
        if (user == null){
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Username does not exist");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Check password comparing submitted password with encrypted hash
        if (BCrypt.checkpw(password, user.getPasswordHash())) {

            // ADD USER DATA TO SESSION (only after password check)
            request.getSession().setAttribute("sessionUserId", user.getId());
            request.getSession().setAttribute("sessionUser", user.getLogin());

            // redirect to '/'
            response.sendRedirect("/");

        }
        // Respond by alert text if NOT authorized
        else {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Password is wrong");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
