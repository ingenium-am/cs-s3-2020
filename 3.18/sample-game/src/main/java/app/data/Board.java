package app.data;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final byte[] ALL_SIZES = { 19, 15, 9 };   // all sizes of boards

    private final Stone[][] grid;                           // array of array of Stone class object
    private final int maxMoves;                             // max available moves (set by constructor)
    private int moves = 0;                                  // move counter (initialized as 0)

    public Board(byte size) {
        this.grid = new Stone[size][size];                  // set empty array of array filled by nulls
        this.maxMoves = size*size;                          // initialize max moves by board size
    }

    // Return list of arrays containing occupied coordinates with player id
    public List<Long[]> getState() {
        // Create empty list parametrized with array of long integers
        List<Long[]> newList = new ArrayList<>();

        // Loop over grid - rows and columns
        for (byte y = 0; y < grid.length; y++) {
            for (byte x = 0; x < grid[y].length; x++) {

                // If element of (x,y) has a Stone - NOT null
                if (grid[y][x] != null) {
                    Stone stone = grid[y][x];

                    // Cast byte coords to long to put with playerId
                    Long[] playerId = { (long) x, (long) y, stone.getPlayerId() };
                    // Add array to list
                    newList.add(playerId);
                }
            }
        }
        return newList;
    }

    // Place a Stone by (x,y) coordinates
    public void setCell(byte x, byte y, Stone stone) {

        // Check if coords are valid and not exceeded maxMoves
        if (moves < maxMoves && coordsAreValid(x, y)) {

            // Check if cell is free - to not overwrite
            if (grid[y][x] == null) {
                grid[y][x] = stone;

                moves++;    // increment move counter
            }
        }
    }

    public int getMoves() {
        return moves;
    }

    public byte getSize() {
        return (byte) grid.length;
    }

    public boolean isFull() {
        return moves == maxMoves;
    }

    public boolean coordsAreValid(byte x, byte y) {
        // Coords are valid if NOT negative and NOT exceeding size of the board
        return x >= 0 && y >= 0 && x < getSize() && y < getSize();  // NOTE: size 9 has last index 8
    }
}
