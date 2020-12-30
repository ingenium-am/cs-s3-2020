package server.websocket;

import app.AppController;
import app.Events;
import app.game.MoveStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import server.shared.DataCache;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* Set class as WebSocket server endpoint by @ServerEndpoint annotation using:
   a) endpoint relative URL (assigning a part of the path to 'gameHex' path parameter of the session)
   b) server configurator class (optionally - see ServerConfig class in this package) */


@ServerEndpoint(value = "/websocket/{gameHex}", configurator = ServerConfig.class)
public class WebsocketServer {

    // Map of playing rooms (key id gameHex passed as endpoint path parameter)
    private static final Map<String, Room> rooms = new HashMap<>();

    // Set triggering on connection open by @OnOpen annotation
    @OnOpen
    public void handleOpen(Session session, EndpointConfig config) {

        // Get HttpSession attributes using custom config (ServerConfig)
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        long userId = (long) httpSession.getAttribute("sessionUserId");
        String userName = (String) httpSession.getAttribute("sessionUser");

        // Get path parameter by 'gameHex' variable (see @ServerEndpoint value)
        String gameHex = session.getPathParameters().get("gameHex");

        Room room = rooms.get(gameHex);             // get Room by gameHex
        if (room == null) {                         // if Room is NOT found
            room = Room.open(userId, session);              // create a new Room for player
            rooms.put(gameHex, room);                       // put created Room into rooms map
        } else {                                    // if Room IS found
            room.join(userId, session);                     // join player to that room
        }

        // Initialize opponent's session as null
        Session opponentSession = null;

        // Check if opponent is still in the Room (may leave after current player is joined to the room)
        if (room.getOnlineCount() > 1) {
            opponentSession = room.getSessionOfOpponent(session);

            String messageOut = new JSONObject()
                    .put("type", Events.JOIN)
                    .put("fromId", userId)
                    .put("fromName", userName)
                    .toString();

            // Send message to opponent about join
            sendMessageWrapper(opponentSession, messageOut);
        }

        // SEND BACK game data after player joined
        String messageBack = new JSONObject()
                .put("type", Events.INIT)
                .put("board", AppController.getSingleton().getBoardJson(gameHex))
                .put("started", DataCache.getGame(gameHex) != null)
                .put("oppOnline", opponentSession != null && opponentSession.isOpen())
                .toString();

        // Send message to joined player
        sendMessageWrapper(session, messageBack);
    }

    // Set triggering on message reception by @OnMessage annotation
    @OnMessage
    public void handleMessage(Session session, String messageIn) {

        // Get path parameter by 'gameHex' variable (see @ServerEndpoint value)
        String gameHex = session.getPathParameters().get("gameHex");

        // Get Room and handle message if it's not null
        Room room = rooms.get(gameHex);
        if (room != null) {
            Session opponentSession = room.getSessionOfOpponent(session);

            // Parse incoming message as JSONObject by external package (org.json)
            JSONObject jsonMessage = new JSONObject(messageIn);

            // Get data by key and try to cast to appropriate type
            String type = (String) jsonMessage.get("type");

            // MOVE message handling
            if (type.equalsIgnoreCase(Events.MOVE.name())) {

                // Get values of the data
                JSONArray coords = jsonMessage.getJSONArray("coords");  // get array by 'coords' key
                byte x = (byte) coords.getInt(0);                       // get first from array as X
                byte y = (byte) coords.getInt(1);                       // get second from array as Y
                long senderId = jsonMessage.getLong("fromId");          // get sender id by 'fromId' key

                // Make a MOVE and get corresponding status
                MoveStatus status = AppController.getSingleton().makeMove(gameHex, x, y, senderId);

                // If an error occurred
                if (status == null) {
                    String message = new JSONObject().put("type", Events.ERROR).toString();
                    sendMessageWrapper(session, message);               // send back to sender
                }

                // If move is accepted
                else if (status.accepted()) {

                    // Add the status to received message
                    String message = jsonMessage
                            .put("status", status)
                            .toString();

                    sendMessageWrapper(session, message);               // send back to sender
                    sendMessageWrapper(opponentSession, message);       // send to opponent
                }

                // SEND BACK otherwise (MOVE is NOT accepted)
                else {
                    // Make a message with type and status only
                    String message = new JSONObject()
                            .put("type", type)
                            .put("status", status)
                            .toString();

                    sendMessageWrapper(session, message);               // send back to sender
                }
            }

            // If INCOMING type is not MOVE
            else {
                sendMessageWrapper(opponentSession, messageIn);         // send to opponent
            }
        }
    }

    // Set triggering on connection close by @OnClose annotation
    @OnClose
    public void handleClose(Session session, CloseReason reason) {

        // Get path parameter by 'gameHex' variable (see @ServerEndpoint value)
        String gameHex = session.getPathParameters().get("gameHex");

        // Get Room and handle event if it's not null
        Room room = rooms.get(gameHex);
        if (room != null) {

            // There is a player who might be informed about disconnection
            if (room.getOnlineCount() > 1) {

                // Get disconnecting player's id by session
                long playerId = room.getIdBySession(session);

                String messageOut = new JSONObject()
                        .put("type", Events.QUIT)
                        .put("fromName", DataCache.getUser(playerId).getLogin())
                        .toString();

                // Get opponent's session  and send the message
                Session opponentSession = room.getSessionOfOpponent(session);
                sendMessageWrapper(opponentSession, messageOut);

                // Remove disconnecting player's session from room
                room.removeSession(playerId);
            }

            // Player closing connection is the last in the room
            else {
                // Remove Room from map
                rooms.remove(gameHex);
            }
        }
    }

    // Set triggering on error by @OnError annotation
    @OnError
    public void handleError(Session session, Throwable throwable) {

        // Get path parameter by 'gameHex' variable (see @ServerEndpoint value)
        String gameHex = session.getPathParameters().get("gameHex");

        // Get Room and handle event if it's not null
        Room room = rooms.get(gameHex);
        if (room != null) {

            String messageOut = new JSONObject()
                    .put("type", Events.ERROR)
                    .put("message", "Websocket Server Error!")
                    .toString();

            // Send message to all sessions
            for (Session roomSession : room.getSessions()) {
                sendMessageWrapper(roomSession, messageOut);
            }
        }
    }


    // * PRIVATE METHODS * //

    // Wrap message send by try/catch block and session checking
    private void sendMessageWrapper(Session session, String message) {

        // Send message if session is NOT null and OPEN
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);

            } catch (IOException e) {
                // Comment out to hide stacktrace
                e.printStackTrace();
            }
        }
    }
}
