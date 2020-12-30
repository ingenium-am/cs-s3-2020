package server.servlets;

import app.data.User;
import dbservices.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;
import server.shared.DataCache;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// See servlet mapping for URLs - web.xml
public class SignUp extends HttpServlet {

    // Handle GET requests
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set 'signup.html' as URL and forward
        RequestDispatcher rd = request.getRequestDispatcher("signup.html");
        rd.forward(request, response);
    }

    // Handle POST requests
    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        // Get request parameters (see signin.html)
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        // If login is invalid - respond by alert text
        if (!isValidLogin(login)) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Invalid username!");
            response.getWriter().println("<br>");
            response.getWriter().println("Allowed 3-16 alphanumeric chars, underscore and [.-] in between.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // If password is invalid - respond by alert text
        if (!isValidPassword(password)) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Invalid password!");
            response.getWriter().println("<br>");
            response.getWriter().println("Allowed 3-20 non-whitespace characters.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Remove leading and trailing whitespace from login if validated
        login = login.trim();

        // Generate encrypted hash of password
        String salt = BCrypt.gensalt(12);
        String passwordHash = BCrypt.hashpw(password, salt);

        // Try to store login and password
        long userId = UserDAO.getSingleton().insertUser(login, passwordHash);

        // If NOT stored (negative integer id) - respond by alert text and redirect
        if (userId < 0) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Username already exists");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Redirect to '/signup' (GET) after 2s
            response.addHeader("REFRESH", "2;URL=/signup");
            return;
        }

        // User account is successfully stored - update cache
        DataCache.updateUsersCache(new User(userId, login, passwordHash));

        // Respond by info text and redirect
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().printf("Successfully signed up: %s\n", login);
        response.setStatus(HttpServletResponse.SC_OK);
        // Redirect to '/' (GET) after 2s
        response.addHeader("REFRESH", "2;URL=/");
    }


    // * PRIVATE METHODS * //

    private boolean isValidLogin(String login) {
        // Uses Regular Expressions to match 3-16 or more alphanumeric chars, underscore and [.-] in between
        // See SQL column for length limitations
        return login.matches("^[\\w][\\w.-]{2,15}[\\w]$");
    }

    private boolean isValidPassword(String password) {
        // Uses Regular Expressions to match 3-20 or more non-whitespace chars
        return password.matches("^[\\S]{3,20}$");
    }
}