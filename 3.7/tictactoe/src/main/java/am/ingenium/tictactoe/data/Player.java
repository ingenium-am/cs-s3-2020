package am.ingenium.tictactoe.data;

public class Player {

    private final int id;
    private final String name;

    public Player(int id) {
        this(id, "Player" + id);
    }

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
