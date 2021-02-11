package app.data;

public class Move {

    private final byte x;
    private final byte y;
    private final long playerID;

    public Move(byte x, byte y, long playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public long getPlayerID() {
        return playerID;
    }
}
