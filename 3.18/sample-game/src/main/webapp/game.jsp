<%@ page import="app.game.Game" %>
<%@ page import="app.util.DateUtils" %>
<%@ page import="server.shared.DataCache" %>
<%@ page import="app.data.User" %>
<%@ page import="app.game.GameState" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // * GET AND CREATE ALL NECESSARY DATA TO COMPILE AN HTML SERVER-SIDE * //

    // Get session attributes stored by servlets
    Game game = (Game) session.getAttribute("game");
    byte stoneIndex = (byte) session.getAttribute("stoneIndex");

    long localPlayerId = (long) session.getAttribute("sessionUserId");
    String localPlayerName = (String) session.getAttribute("sessionUser");


    String[] stones = { "Black", "White" };                             // Black moves FIRST

    String localPlayerStone = stones[stoneIndex];
    String remotePlayerStone = stones[(stoneIndex + 1) % 2];            // stoneIndex = 0 -> 1, 1 -> 0

    String localPlayerStoneSrc = String.format("%s/img/%s.png",         // 1 - contextPath, 2 - image name
            request.getContextPath(), localPlayerStone.toLowerCase());  // image name placed lowercase

    String remotePlayerStoneSrc = String.format("%s/img/%s.png",        // 1 - contextPath, 2 - image name
            request.getContextPath(), remotePlayerStone.toLowerCase()); // image name placed lowercase


    // Check if opponent exists - game is started or closed
    String remotePlayerName;

    long remotePlayerId = game.getOpponentId(localPlayerId);
    if (remotePlayerId > 0) {
        User player2 = DataCache.getUser(remotePlayerId);
        if (player2 != null) {
            remotePlayerName = player2.getLogin();
        } else {
            remotePlayerName = "#" + remotePlayerId + " unknown";       // user data may be deleted
        }
    } else {
        remotePlayerName = "... waiting for opponent";
    }

    // Get turn ID and indicator image
    boolean localPlayersTurn = localPlayerId == game.getTurn();
    String turnIndicatorImgSrc = request.getContextPath() + "/img/arrow.png";
    boolean draw = false;

    // If the Game is CLOSED
    GameState gameState = game.getState();
    if (gameState == GameState.CLOSED) {

        // If there is a winner - make turn indicator a winner indicator
        if (game.win()) {
            // ID is opposite of turn
            localPlayersTurn = localPlayerId != game.getTurn();
            turnIndicatorImgSrc = request.getContextPath() + "/img/star.png";
        }
        // If closed but there is no winner
        else {
            // Set draw and use for visibility of img indicators
            draw = true;
        }
    }

    // Set title and subtitle
    byte boardSize = game.getBoard().getSize();
    String title = boardSize + "x" + boardSize + " Table";

    String creationDate = DateUtils.dateToBriefStamp(game.getCreationDate());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <meta name="viewport" content="width=device-width" />
    <title>Game</title>
    <script>
        // THIS PART OF THE SCRIPT MUST BE INTERNAL
        // to dynamically set variables server-side, avoiding additional requests from client

        const boardSize = <%=game.getBoard().getSize()%>;       // set as Number
        const gameHex = "<%=game.getHex()%>";                   // set as String (quoted)

        const localPlayerId = <%=localPlayerId%>;               // set as Number
        const localPlayerName = "<%=localPlayerName%>";         // set as String (quoted)

        const localPlayerStone = new Image();
        const remotePlayerStone = new Image();
        localPlayerStone.src = "<%=localPlayerStoneSrc%>";      // set as String (quoted)
        remotePlayerStone.src = "<%=remotePlayerStoneSrc%>";    // set as String (quoted)

        // See static 'scripts/game.js' file (import is below) for the rest of the client-side code
    </script>

    <%-- Load 'styles.css' static stylesheet file --%>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css" type="text/css" media="screen" />
    <%-- Load 'game.js' static script file --%>
    <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/game.js"></script>
</head>
<body>
    <%-- Header of the page --%>
    <div id="header" class="center">
        <h3 id="title"><%=title%></h3>
        <p id="title-date"><%=creationDate%></p>
        <p id="subtitle" class="info">Get a line of five or more stones for a win</p>
    </div>

    <%-- Game canvas and info line --%>
    <div id="game" class="center">

        <div id="grid" class="grid center">
            <canvas id="grid-canvas" class="center"></canvas>
        </div>

        <div id="info" class="info-init">Connecting to room...</div>
    </div>

    <%-- Players board with status indicators --%>
    <div id="footer" class="center">
        <table id="players">
            <tbody>
                <%-- Local player (whos side is running this code) is the first row --%>
                <tr id="local-player">
                    <td id="lp-turn">
                        <img id="lp-turn-img" src="<%=turnIndicatorImgSrc%>" alt="Turn"
                             style="<%=!draw && localPlayersTurn ? "display: block" : "display: none"%>">
                    </td>
                    <td id="lp-stone">
                        <img src="<%=localPlayerStoneSrc%>" alt="<%=localPlayerStone%>">
                    </td>
                    <td id="lp-name" class="local-user-name"><%=localPlayerName%></td>
                </tr>
                <%-- Remote player is the second row --%>
                <tr id="remote-player">
                    <td id="rp-turn">
                        <img id="rp-turn-img" src="<%=turnIndicatorImgSrc%>" alt="Turn"
                             style="<%=draw || localPlayersTurn ? "display: none" : "display: block"%>">
                    </td>
                    <td id="rp-stone">
                        <img src="<%=remotePlayerStoneSrc%>" alt="<%=remotePlayerStone%>">
                    </td>
                    <td id="rp-name" class=<%=remotePlayerId == 0 ? "unknown" : ""%>><%=remotePlayerName%></td>
                </tr>
            </tbody>
        </table>
    </div>

    <%-- Close button as a form --%>
    <div id="close" class="center">
        <form method="POST" action="<%=request.getContextPath()%>/lobby">
            <input type="SUBMIT" value="Close">
        </form>
    </div>

</body>
</html>