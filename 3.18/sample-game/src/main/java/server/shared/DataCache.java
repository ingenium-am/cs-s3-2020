package server.shared;

import app.data.User;
import app.game.Game;
import dbservices.dao.GameDAO;
import dbservices.dao.UserDAO;

import java.util.HashMap;
import java.util.Map;

public class DataCache {

    // Needs to be updated runtime if a new User created or deleted
    private static final Map<Long, User> usersCache;

    // Needs to be updated runtime if a new Game created or deleted
    private static final Map<String, Game> gamesCache;


    static {
        // Order is strict - gameCache needs usersCache as a dependency
        usersCache = UserDAO.getSingleton().getAllUsers();
        gamesCache = GameDAO.getSingleton().getAllGames();
    }

    public static Map<Long, User> getUsersCache() {
        return new HashMap<>(usersCache);
    }

    public static Map<String, Game> getGamesCache() {
        return new HashMap<>(gamesCache);
    }

    public static User getUser(long id) {
        return usersCache.get(id);
    }

    public static Game getGame(String hex) {
        return gamesCache.get(hex);
    }

    public static void updateUsersCache(User user) {
        usersCache.put(user.getId(), user);
    }

    public static void updateGamesCache(Game game) {
        gamesCache.put(game.getHex(), game);
    }
}
