<%@ page import="java.util.Map" %>
<%@ page import="server.shared.DataCache" %>
<%@ page import="app.game.Game" %>
<%@ page import="app.AppController" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="app.data.User" %>
<%@ page import="app.util.DateUtils" %>
<%@ page import="app.data.Board" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // * GET ALL NECESSARY DATA TO COMPILE AN HTML SERVER-SIDE * //

    // Get session attributes stored by servlets
    long localPlayerId = (Long) session.getAttribute("sessionUserId");
    String localPlayerName = (String) session.getAttribute("sessionUser");

    // Get caches and waiting games
    Map<String, Game> gamesCache = DataCache.getGamesCache();
    Map<Long, User> usersCache = DataCache.getUsersCache();
    Map<String, Game> waitingGames = AppController.getSingleton().getWaitingGames();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <meta name="viewport" content="width=device-width" />
    <title>Lobby | Game</title>

    <%-- Load 'styles.css' static stylesheet file --%>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css" type="text/css" media="screen" />
    <%-- Load 'lobby.js' static script file --%>
    <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/lobby.js"></script>
</head>
<body>
    <%-- Header of the page - <username> | Logout --%>
    <div class="center">
        <p>
            <span class="local-user-name">
                <%=localPlayerName%>
            </span> | <a href="<%=request.getContextPath()%>/logout">Logout</a>
        </p>
    </div>

    <%-- 'Loading a game' group --%>
    <div id="load-game" class="center">

        <%-- For corresponding servlet see Servlet Mapping - WEB-INF/web.xml --%>
        <form method="POST" action="<%=request.getContextPath()%>/game">
            <%-- Title of the group --%>
            <h4>Load a Game</h4>

            <%-- SELECTOR AND SUBMIT BUTTON --%>
            <label class="select">

                <%-- Values of 'name' and selected option will be attached to POST (see form) --%>
                <select id="load-game-selector" name="load-game"
                        size="1" onchange="buttonEnabler(this.getAttribute('id'))">

                    <%-- Default disabled option as a placeholder --%>
                    <option value="" selected disabled >Select Game</option>

                    <%  // INLINE JSP CODE

                        // Get all games from cache with localPlayer (sessionUser) and sort by date
                        List<Game> ownGames = gamesCache.values().stream()
                                .filter(g -> g.hasPlayer(localPlayerId))
                                .sorted(Comparator.comparing(Game::getCreationDate))
                                .collect(Collectors.toList());

                        // Loop over localPlayer's games
                        for (Game game : ownGames) {

                            String gameDate = DateUtils.dateToBriefStamp(game.getCreationDate());
                            long opponentId = game.getOpponentId(localPlayerId);
                            String opponentName = usersCache.get(opponentId).getLogin();

                            // Create text for current game in loop
                            String innerText = "[ " + gameDate + " ] " + " @" + opponentName;
                    %>

                    <%-- Put options with injected values as a part of the JSP loop --%>
                    <option value="<%=game.getHex()%>"><%=innerText %></option>

                    <%
                        }   // Closing scope of 'for' loop
                    %>
                </select>

            </label>
            <%-- Load button - disabled for default option --%>
            <input id="load-button" type="submit" value="Load" disabled>
        </form>
    </div>

    <%-- 'Join a game' group --%>
    <div id="waiting-games" class="center">

        <%-- For corresponding servlet see Servlet Mapping - WEB-INF/web.xml --%>
        <form method="POST" action="<%=request.getContextPath()%>/game">
            <%-- Title of the group --%>
            <h4>Join a Game</h4>

            <%-- SELECTOR AND SUBMIT BUTTON --%>
            <label class="select">

                <%-- Values of 'name' and selected option will be attached to POST (see form) --%>
                <select id="join-game-selector" name="join-game"
                        size="1" onchange="buttonEnabler(this.getAttribute('id'))">

                    <%-- Default disabled option as a placeholder --%>
                    <option value="" selected disabled >Select Game</option>

                    <%  // INLINE JSP CODE

                        // Loop over waiting games
                        for (Game game : waitingGames.values()) {
                            String creatorName = DataCache.getUser(game.getPlayer1Id()).getLogin();

                            // Skip games created by localPlayer (sessionUser)
                            if (!creatorName.equals(localPlayerName)) {

                                byte boardSize = game.getBoard().getSize();

                                // Create text for current game in loop
                                String innerText = boardSize + "x" + boardSize + " @" + creatorName;
                    %>

                    <%-- Put options with injected values as a part of the JSP loop --%>
                    <option value="<%=game.getHex()%>"><%=innerText%></option>

                    <%
                            }   // Closing scope 'if' conditional
                        }       // Closing scope of 'for' loop
                    %>
                </select>
            </label>
            <%-- Join button - disabled for default option --%>
            <input id="join-button" type="submit" value="Join" disabled>
        </form>
    </div>

    <%-- 'Create a new game' group --%>
    <div id="new-game" class="center">

        <%-- For corresponding servlet see Servlet Mapping - WEB-INF/web.xml --%>
        <form method="POST" action="<%=request.getContextPath()%>/game">
            <%-- Title of the group --%>
            <h4>Create a new Game</h4>

            <%-- SELECTOR AND SUBMIT BUTTON --%>
            <label class="select">

                <%-- Values of 'name' and selected option will be attached to POST (see form) --%>
                <select id="board-size" name="board-size" size="1">
                    <%  // INLINE JSP CODE

                        // Loop over available board sizes
                        for (byte size : Board.ALL_SIZES) {
                    %>

                    <%-- Put options with injected values as a part of the JSP loop --%>
                    <option value="<%=size%>"><%=size + "x" + size%></option>

                    <%
                        }   // Closing scope of 'for' loop
                    %>
                </select>
            </label>
            <%-- Create button - disabled for default option --%>
            <input type="submit" value="Create">
        </form>
    </div>
</body>
</html>
