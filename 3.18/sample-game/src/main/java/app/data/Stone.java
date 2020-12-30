package app.data;

import java.util.Date;

public class Stone {

    private final long playerId;
    private final Date date;

    public Stone(long playerId, Date date) {
        this.playerId = playerId;
        this.date = date;
    }

    public long getPlayerId() {
        return playerId;
    }

    public Date getDate() {
        return date;
    }
}