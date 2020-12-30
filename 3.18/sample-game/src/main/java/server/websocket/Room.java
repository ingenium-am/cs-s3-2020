package server.websocket;

import javax.websocket.Session;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// Wrapper for WebSocket peer-to-peer connection emulation
public class Room {

    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>(2);

    private Room(long playerId, Session session) {
        sessionMap.put(playerId, session);
    }

    public static Room open(long playerId, Session session) {
        return new Room(playerId, session);
    }

    public void join(long playerId, Session session) {
        sessionMap.put(playerId, session);
    }

    public long getIdBySession(Session session) {
        return sessionMap.entrySet().stream()
                .filter(entry -> entry.getValue() == session)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse((long) -1);
    }

    public Session getSessionOfOpponent(Session session) {
        return sessionMap.values().stream()
                .filter(s -> !session.equals(s))
                .findFirst()
                .orElse(null);
    }

    public void removeSession(long playerId) {
        sessionMap.remove(playerId);
    }

    public Collection<Session> getSessions() {
        return sessionMap.values();
    }

    public byte getOnlineCount() {
        return (byte) sessionMap.size();
    }
}
