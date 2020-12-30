package server.servlets;

import app.AppController;
import app.game.Game;
import server.shared.DataCache;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

// See servlet mapping for URLs - web.xml
public class GameServlet extends HttpServlet {

    // Handle GET requests
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get session for reuse
        HttpSession session = request.getSession();

        // Check if 'sessionUser' attribute isn't set for session (not signed in)
        if (session.getAttribute("sessionUser") == null) {
            response.sendRedirect("/signin");                   // redirect to sign in
            return;
        }

        // Get path info - /game/<path-info> (see servlet mapping)
        String pathInfo = request.getPathInfo();

        // If no path info (/game) or path info is empty (/game/)
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect("/lobby");                    // redirect to lobby
            return;
        }

        // Get path string trimming first char ("/") as gameHex
        String pathParam = pathInfo.substring(1);               // from 1 to the end (no end pos)

        // Get game from session attribute
        Game game = (Game) session.getAttribute("game");

        // If game is NOT passed as attribute (direct URL is used)
        if (game == null) {

            // Get game from cache
            game = DataCache.getGame(pathParam);

            // If game still NOT found
            if (game == null) {
                response.sendRedirect("/lobby");                // redirect to lobby
                return;
            }
        }

        // If game hex is matching with path parameter
        if (pathParam.equals(game.getHex())) {

            // Get player ID from session attribute - here game IS found
            long sessionUserId = (long) session.getAttribute("sessionUserId");

            // Set required attributes to session
            session.setAttribute("stoneIndex", sessionUserId == game.getPlayer1Id() ? (byte) 0 : (byte) 1);
            session.setAttribute("game", game);

            // Set 'game.jsp' as URL and forward
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);

        } else {
            response.sendRedirect("/lobby");                    // redirect to lobby
        }
    }

    // Handle POST requests
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Get session for reuse
        HttpSession session = request.getSession();

        // Get session attribute (cast boxed type to return null if NOT found)
        Long sessionUserId = (Long) session.getAttribute("sessionUserId");

        // Check if authorized (session attribute is set) - respond if NOT
        if (sessionUserId == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("Not authorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // *** NEW GAME CREATION *** //

        // Check parameter of request attached by submitting a form (see lobby.jsp)
        String boardSizeStr = request.getParameter("board-size");
        if (boardSizeStr != null) {

            // Parse string parameter as a byte value
            byte boardSize = Byte.parseByte(boardSizeStr);

            // Create a new game passing board size and creator's ID
            Game game = AppController.getSingleton().createGame(boardSize, sessionUserId);

            // Set session attributes
            session.setAttribute("stoneIndex", (byte) 0);       // 0 - means first player (black stone)
            session.setAttribute("game", game);

            // Redirect POST as GET (loopback to the same servlet)
            response.sendRedirect("/game/" + game.getHex());
            return;
        }

        // *** JOIN A GAME *** //

        // Check parameter of request attached by submitting a form (see lobby.jsp)
        String joinGameHex = request.getParameter("join-game");
        if (joinGameHex != null) {

            // Get controller for reuse
            AppController appController = AppController.getSingleton();

            // Get game from waitingGames request parameter
            Game game = appController.getWaitingGames().get(joinGameHex);

            // Start game and get result
            boolean started = appController.startGame(game, sessionUserId);

            // If successfully started
            if (started) {
                // Set session attributes
                session.setAttribute("stoneIndex", (byte) 1);   // 1 - means second player (white stone)
                session.setAttribute("game", game);

                // Redirect POST as GET (loopback to the same servlet)
                response.sendRedirect("/game/" + game.getHex());
            } else {
                response.sendRedirect("/lobby");                // redirect to lobby
            }
            return;
        }

        // *** LOAD A GAME *** //

        // Check parameter of request attached by submitting a form (see lobby.jsp)
        String loadGameHex = request.getParameter("load-game");
        if (loadGameHex != null) {

            // Get game from cache - must be already started
            Game game = DataCache.getGame(loadGameHex);

            // Check if user is one of players of the game
            if (game.hasPlayer(sessionUserId)) {

                // Set session attributes (stone index - 0 if player1, 1 if player2)
                session.setAttribute("stoneIndex", sessionUserId == game.getPlayer1Id() ? (byte) 0 : (byte) 1);
                session.setAttribute("game", game);

                // Redirect POST as GET (loopback to the same servlet)
                response.sendRedirect("/game/" + game.getHex());
            } else {
                response.sendRedirect("/lobby");                // redirect to lobby
            }
            return;
        }

        // ALL UNMATCHED CASES
        response.sendRedirect("/");
    }
}
