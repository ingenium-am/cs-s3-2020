package app.game;

// This enum has a property
public enum MoveStatus {

    // Each value takes a property by declaration

    INVALID_MOVE (false),
    NOT_STARTED (false),
    NOT_AUTHORIZED (false),
    NOT_YOUR_TURN (false),
    FINISHED(false),
    SUCCESS (true),
    DRAW (true),
    WIN (true);

    // Each value must have a property of boolean type
    private final boolean accepted;

    // Constructor is private and invoking implicitly
    MoveStatus(boolean accepted) {
        this.accepted = accepted;
    }

    // Getter of the property of a value
    public boolean accepted() {
        return accepted;
    }
}
